package com.github.tilcob.game;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class TexturePackerTool {
    public static void main(String[] args) {
        String inputDir = "assets_raw/indicators";
        String outputDir = "assets/graphics";
        String packFileName = "indicators";

        TexturePacker.process(inputDir, outputDir, packFileName);
    }
}
