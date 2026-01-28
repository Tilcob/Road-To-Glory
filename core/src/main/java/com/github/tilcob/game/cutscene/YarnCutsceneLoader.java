package com.github.tilcob.game.cutscene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.yarn.YarnParser;

import java.util.*;

public class YarnCutsceneLoader {

    public CutsceneData load(String cutsceneId, FileHandle fileHandle) {
        if (fileHandle == null || !fileHandle.exists()) {
            Gdx.app.error("YarnCutsceneLoader", "Cutscene file missing: "
                + (fileHandle == null ? "null" : fileHandle.path()));
            return CutsceneData.empty(cutsceneId);
        }
        String content;
        try {
            content = fileHandle.readString("UTF-8");
        } catch (Exception e) {
            Gdx.app.error("YarnCutsceneLoader", "Failed to read cutscene file: " + fileHandle.path(), e);
            return CutsceneData.empty(cutsceneId);
        }

        List<YarnParser.YarnNodeRaw> parsedNodes = YarnParser.parse(content);
        YarnParser.YarnNodeRaw startNode = findStartNode(parsedNodes);
        Array<String> startLines = startNode == null ? new Array<>() : toGdxArrayTrimmed(startNode.bodyLines());

        Array<CutsceneNode> nodes = new Array<>();
        for (YarnParser.YarnNodeRaw n : parsedNodes) {
            nodes.add(new CutsceneNode(n.id(), toGdxArrayTrimmed(n.bodyLines())));
        }

        ObjectMap<String, CutsceneNode> nodesById = new ObjectMap<>();
        for (CutsceneNode node : nodes) {
            if (node != null && node.id() != null) {
                nodesById.put(node.id(), node);
            }
        }

        return new CutsceneData(cutsceneId, startLines, nodesById);
    }

    private static YarnParser.YarnNodeRaw findStartNode(List<YarnParser.YarnNodeRaw> parsedNodes) {
        for (YarnParser.YarnNodeRaw node : parsedNodes) {
            if (node.tags().contains("root") || node.tags().contains("start")) return node;
        }
        for (YarnParser.YarnNodeRaw node : parsedNodes) {
            if ("start".equalsIgnoreCase(node.id())) return node;
        }
        return parsedNodes.isEmpty() ? null : parsedNodes.get(0);
    }

    private static Array<String> toGdxArrayTrimmed(List<String> lines) {
        Array<String> array = new Array<>();
        if (lines == null) return array;
        for (String line : lines) {
            if (line == null) continue;
            array.add(line.trim());
        }
        return array;
    }
}
