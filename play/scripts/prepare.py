"""Prepara les imatges de Google Play a partir de play/originals (que no es modifiquen).

Només fa el que Play exigeix: treu el canal alfa (tot és opac), baixa la feature graphic de
2048x1000 a 1024x500 (×0,5 exacte) i la icona de 1024 a 512.
Ús: play/.venv/bin/python play/scripts/prepare.py
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
SRC, OUT = ROOT / "originals", ROOT / "out"
LOCALES = {"ca": "ca", "es": "es-ES", "en": "en-US"}


def save(im, path, size=None):
  assert im.getchannel("A").getextrema() == (255, 255), f"{path.name}: té transparència"
  im = im.convert("RGB")
  if size:
    im = im.resize(size, Image.LANCZOS)
  path.parent.mkdir(parents=True, exist_ok=True)
  im.save(path, optimize=True)
  print(path.relative_to(ROOT), im.size, path.stat().st_size // 1024, "KB")


save(Image.open(SRC / "play-icon-512.png"), OUT / "icon-512.png", (512, 512))
for src in sorted(SRC.glob("*.png")):
  suffix = src.stem.rsplit("-", 1)[-1]
  if suffix not in LOCALES or src.stem == "play-icon-512":
    continue
  locale = LOCALES[suffix]
  if src.stem.startswith("feature-graphic"):
    save(Image.open(src), OUT / locale / "feature-1024x500.png", (1024, 500))
  else:
    number = src.stem.split("-")[1]
    save(Image.open(src), OUT / locale / f"phone-{number}.png")
