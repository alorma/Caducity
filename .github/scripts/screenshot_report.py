#!/usr/bin/env python3
"""
Builds a PR comment from the screenshot test results of every module.

Reads the JUnit XML written by validateDebugScreenshotTest in each module, copies the
reference, new and diff images of every failing test into an output directory (to be
pushed to a companion branch), and writes:
  - comment.md: markdown with an image table per failing test. Image URLs use the
    {IMAGES_URL} placeholder, replaced once the images are pushed.
  - update-tasks.txt: Gradle tasks and --tests filters to regenerate only the
    references of the failing tests, one argument per line. Test method names
    can't contain underscores, they separate the method from the preview name.
"""
import argparse
import glob
import os
import re
import shutil
import xml.etree.ElementTree as ET

RESULTS_GLOB = "*/build/test-results/validateDebugScreenshotTest/*.xml"
REFERENCE_DIR = "/src/screenshotTestDebug/reference/"
RENDERED_DIR = "/build/outputs/screenshotTest-results/preview/debug/rendered/"
MARKER = "<!-- screenshot-tests-report -->"


def parse_failures(results_glob):
    results = sorted(glob.glob(results_glob))
    if not results:
        raise SystemExit(f"No test results match {results_glob}, the tests didn't run")
    failures = []
    for path in results:
        module = os.path.normpath(path).split(os.sep)[0]
        for case in ET.parse(path).getroot().iter("testcase"):
            problem = case.find("failure")
            if problem is None:
                problem = case.find("error")
            if problem is None:
                continue
            message = problem.get("message") or problem.text or ""
            name = case.get("name")
            failure = {
                "module": module,
                "test_class": case.get("classname").split(".")[-1],
                # Multi-preview tests are named "<method>_<preview name>_{<parameters>}"
                "test_name": re.split(r"[_ ]", name)[0],
                "image_name": re.sub(r"[^A-Za-z0-9_.-]+", "_", name).strip("_"),
                "summary": message.splitlines()[0] if message else "Unknown error",
                "reference": None,
                "actual": None,
                "diff": None,
            }
            expected = re.search(r"Expected: (\S+)", message)
            actual = re.search(r"Actual: (\S+)", message)
            diff = re.search(r"Diff Image: (\S+)", message)
            missing = re.search(r"Reference image file does not exist \((\S+?)\)", message)
            if expected:
                failure["reference"] = expected.group(1)
            if actual:
                failure["actual"] = actual.group(1)
            if diff:
                failure["diff"] = diff.group(1)
            if missing:
                failure["summary"] = "New screenshot, no reference image yet"
                failure["actual"] = missing.group(1).replace(REFERENCE_DIR, RENDERED_DIR)
            failures.append(failure)
    return failures


def copy_image(source, images_dir, name):
    if not source or not os.path.isfile(source):
        return None
    os.makedirs(images_dir, exist_ok=True)
    shutil.copyfile(source, os.path.join(images_dir, name))
    return name


def image_cell(name):
    return f'<img src="{{IMAGES_URL}}/{name}?raw=true" width="250">' if name else "—"


def build_comment(failures, images_dir, run_url):
    lines = [MARKER]
    if not failures:
        lines.append("### ✅ Screenshot tests passed")
        lines.append("")
        lines.append(f"All screenshots match their references. [Workflow run]({run_url})")
        return "\n".join(lines)

    lines.append(f"### ❌ {len(failures)} screenshot test(s) failed")
    lines.append("")
    lines.append(
        "If the changes are expected, add the `update-screenshots` label to this PR "
        "to regenerate the references in a new commit."
    )
    lines.append("")
    for failure in failures:
        test_id = f"{failure['module']}:{failure['test_class']}.{failure['test_name']}"
        file_id = f"{failure['module']}.{failure['test_class']}.{failure['image_name']}"
        reference = copy_image(failure["reference"], images_dir, f"{file_id}.reference.png")
        actual = copy_image(failure["actual"], images_dir, f"{file_id}.new.png")
        diff = copy_image(failure["diff"], images_dir, f"{file_id}.diff.png")
        lines.append(f"<details open><summary><b>{test_id}</b> ({failure['image_name']}): {failure['summary']}</summary>")
        lines.append("")
        lines.append("| Reference | New | Diff |")
        lines.append("|---|---|---|")
        lines.append(f"| {image_cell(reference)} | {image_cell(actual)} | {image_cell(diff)} |")
        lines.append("")
        lines.append("</details>")
        lines.append("")
    lines.append(f"[Workflow run]({run_url})")
    return "\n".join(lines)


def build_update_tasks(failures):
    args = []
    for module in sorted({failure["module"] for failure in failures}):
        args.append(f":{module}:updateDebugScreenshotTest")
        # Trailing wildcard so the filter matches every preview of the method
        filters = {f"*.{f['test_class']}.{f['test_name']}*" for f in failures if f["module"] == module}
        for test_filter in sorted(filters):
            args.extend(["--tests", test_filter])
    return args


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--results-glob", default=RESULTS_GLOB)
    parser.add_argument("--images-dir", required=True)
    parser.add_argument("--output-dir", required=True)
    parser.add_argument("--run-url", required=True)
    args = parser.parse_args()

    failures = parse_failures(args.results_glob)
    os.makedirs(args.output_dir, exist_ok=True)
    with open(os.path.join(args.output_dir, "comment.md"), "w") as f:
        f.write(build_comment(failures, args.images_dir, args.run_url))
    with open(os.path.join(args.output_dir, "update-tasks.txt"), "w") as f:
        f.write("\n".join(build_update_tasks(failures)) + "\n")
    print(f"{len(failures)} failing screenshot test(s)")


if __name__ == "__main__":
    main()
