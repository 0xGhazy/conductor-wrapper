import React from "react";
import { ReactFlow, Background, Controls, MiniMap } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useStudio } from "../Utils/store";
import type { NodeMouseHandler, EdgeMouseHandler } from "@xyflow/react";

export default function Canvas() {
  const {
    nodes,
    edges,
    onConnect,
    setSelectedNode,
    setSelectedEdge,
    setNodePosition,
  } = useStudio();

  const onNodeClick: NodeMouseHandler = (_, node) => setSelectedNode(node.id);
  const onEdgeClick: EdgeMouseHandler = (_, edge) => setSelectedEdge(edge.id);
  const onNodeDragStop: NodeMouseHandler = (_, node) => {
    setNodePosition(node.id, node.position);
  };

  return (
    <div style={{ height: "calc(100vh - 64px)" }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onConnect={onConnect}
        onNodeClick={onNodeClick}
        onEdgeClick={onEdgeClick}
        onNodeDragStop={onNodeDragStop}
        onPaneClick={() => {
          setSelectedNode(undefined);
          setSelectedEdge(undefined);
        }}
        fitView
      >
        <MiniMap />
        <Controls />
        <Background />
      </ReactFlow>
    </div>
  );
}
