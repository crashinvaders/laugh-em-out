#!/usr/bin/sh

# Find the installation dir.
# A good solution that can withstand symbolic links.
# https://stackoverflow.com/a/17744637/3802890
SCRIPT_DIR="$(dirname "$(readlink -f -- "$0")")"

aseprite -b "$SCRIPT_DIR"/laugh-em-out.ase --save-as "$SCRIPT_DIR"/images/{slice}.png

