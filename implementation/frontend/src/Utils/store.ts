import { create } from "zustand";
import { nanoid } from "nanoid";
import type { Node, Edge, Connection } from "@xyflow/react";
import { MarkerType } from "@xyflow/react";
import { BlueprintSchema } from "./blueprintSchema";
import type { Blueprint } from "./blueprintSchema";

type PhaseKey = "before" | "during" | "after";

type StudioState = {
  nodes: Node[];
  edges: Edge[];
  selectedNodeId?: string;
  selectedEdgeId?: string;

  // selection
  setSelectedNode: (id?: string) => void;
  setSelectedEdge: (id?: string) => void;

  // node ops
  addStatus: (label?: string, pos?: { x: number; y: number }) => void;
  renameStatus: (id: string, label: string) => void;
  setNodePosition: (id: string, pos: { x: number; y: number }) => void;
  removeSelected: () => void;

  // edge ops
  onConnect: (c: Connection) => void;
  renameTransition: (id: string, label: string) => void;
  setTransitionGuard: (id: string, guard?: string) => void;
  setTransitionPhase: (id: string, key: PhaseKey, value: string) => void;

  // import/export
  exportBlueprint: () => Blueprint;
  importBlueprint: (bp: Blueprint) => void;
};

export const useStudio = create<StudioState>((set, get) => ({
  // initial nodes/edges: "Not Started" -> "Close Task" -> "Done"
  nodes: [
    {
      id: "not-started",
      position: { x: 480, y: 40 },
      data: { label: "Not Started" },
      type: "default",
    },
    {
      id: "done",
      position: { x: 500, y: 260 },
      data: { label: "Done" },
      type: "default",
    },
  ],
  edges: [
    {
      id: "close-task",
      source: "not-started",
      target: "done",
      label: "Close Task",
      markerEnd: { type: MarkerType.ArrowClosed },
      type: "default",
      data: { guard: "", phases: { before: "", during: "", after: "" } },
    },
  ],

  // selection
  setSelectedNode: (id) =>
    set({ selectedNodeId: id, selectedEdgeId: undefined }),
  setSelectedEdge: (id) =>
    set({ selectedEdgeId: id, selectedNodeId: undefined }),

  // node ops
  addStatus: (label = "New status", pos = { x: 120, y: 120 }) =>
    set((s) => ({
      nodes: [
        ...s.nodes,
        { id: nanoid(), data: { label }, position: pos, type: "default" },
      ],
    })),

  renameStatus: (id, label) =>
    set((s) => ({
      nodes: s.nodes.map((n) =>
        n.id === id ? { ...n, data: { ...n.data, label } } : n
      ),
    })),

  setNodePosition: (id, pos) =>
    set((s) => ({
      nodes: s.nodes.map((n) => (n.id === id ? { ...n, position: pos } : n)),
    })),

  removeSelected: () => {
    const { selectedEdgeId, selectedNodeId } = get();
    if (selectedEdgeId) {
      set((s) => ({
        edges: s.edges.filter((e) => e.id !== selectedEdgeId),
        selectedEdgeId: undefined,
      }));
    } else if (selectedNodeId) {
      set((s) => ({
        nodes: s.nodes.filter((n) => n.id !== selectedNodeId),
        edges: s.edges.filter(
          (e) => e.source !== selectedNodeId && e.target !== selectedNodeId
        ),
        selectedNodeId: undefined,
      }));
    }
  },

  // edge ops
  onConnect: (c) =>
    set((s) => {
      if (!c.source || !c.target) return {};
      const id = nanoid();
      return {
        edges: [
          ...s.edges,
          {
            id,
            source: c.source,
            target: c.target,
            label: "Transition",
            markerEnd: { type: MarkerType.ArrowClosed },
            type: "default",
            data: { guard: "", phases: { before: "", during: "", after: "" } },
          },
        ],
      };
    }),

  renameTransition: (id, label) =>
    set((s) => ({
      edges: s.edges.map((e) => (e.id === id ? { ...e, label } : e)),
    })),

  setTransitionGuard: (id, guard) =>
    set((s) => ({
      edges: s.edges.map((e) =>
        e.id === id ? { ...e, data: { ...(e.data || {}), guard } } : e
      ),
    })),

  setTransitionPhase: (id, key, value) =>
    set((s) => ({
      edges: s.edges.map((e) =>
        e.id === id
          ? {
              ...e,
              data: {
                ...(e.data || {}),
                phases: { ...(e.data as any)?.phases, [key]: value },
              },
            }
          : e
      ),
    })),

  // import/export
  exportBlueprint: () => {
    const s = get();
    return BlueprintSchema.parse({
      statuses: s.nodes.map((n) => ({
        id: n.id,
        label: (n.data as any)?.label ?? "",
        position: n.position,
      })),
      transitions: s.edges.map((e) => ({
        id: e.id,
        from: e.source,
        to: e.target,
        label: (e.label as string) || undefined,
        guard: (e.data as any)?.guard,
        phases: (e.data as any)?.phases,
      })),
    });
  },

  importBlueprint: (bp) => {
    const parsed = BlueprintSchema.parse(bp);
    set({
      nodes: parsed.statuses.map((s) => ({
        id: s.id,
        data: { label: s.label },
        position: s.position,
        type: "default",
      })),
      edges: parsed.transitions.map((t) => ({
        id: t.id,
        source: t.from,
        target: t.to,
        label: t.label,
        data: { guard: t.guard, phases: t.phases },
        markerEnd: { type: MarkerType.ArrowClosed },
        type: "default",
      })),
      selectedEdgeId: undefined,
      selectedNodeId: undefined,
    });
  },
}));
