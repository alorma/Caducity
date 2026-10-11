#!/usr/bin/env bash
# Prints markdown with the before/after images of the screenshot references changed between two commits.
# Usage: updated_references.sh <base-sha> <head-sha>
set -euo pipefail

base="$1"
head="$2"
# github.com blob URLs instead of raw.githubusercontent.com, which only serves private repos with a token
blob="https://github.com/$GITHUB_REPOSITORY/blob"

echo "| Screenshot | Before | After |"
echo "|---|---|---|"
git diff --no-renames --name-status "$base" "$head" -- ':(glob)*/src/screenshotTestDebug/reference/**' | while read -r status file; do
  name="${file%%/*}:$(basename "$(dirname "$file")")/$(basename "$file" .png)"
  case "$status" in
    A) before="New" ;;
    *) before="<img src=\"$blob/$base/$file?raw=true\" width=\"250\">" ;;
  esac
  case "$status" in
    D) after="Deleted" ;;
    *) after="<img src=\"$blob/$head/$file?raw=true\" width=\"250\">" ;;
  esac
  echo "| \`$name\` | $before | $after |"
done
