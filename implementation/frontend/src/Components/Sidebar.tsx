import * as React from "react";
import {
  Box,
  Drawer,
  Typography,
  Divider,
  TextField,
  Tabs,
  Tab,
  Stack,
} from "@mui/material";
import { useStudio } from "../Utils/store";

const WIDTH = 360;

export default function Sidebar() {
  const {
    selectedNodeId,
    selectedEdgeId,
    nodes,
    edges,
    setSelectedNode,
    setSelectedEdge,
    renameStatus,
    renameTransition,
    setTransitionGuard,
    setTransitionPhase,
  } = useStudio();

  const node = React.useMemo(
    () => nodes.find((n) => n.id === selectedNodeId),
    [nodes, selectedNodeId]
  );
  const edge = React.useMemo(
    () => edges.find((e) => e.id === selectedEdgeId),
    [edges, selectedEdgeId]
  );

  const [tab, setTab] = React.useState(0);
  const phases = (edge?.data as any)?.phases || {};
  const guard = (edge?.data as any)?.guard || "";

  const close = () => {
    setSelectedNode(undefined);
    setSelectedEdge(undefined);
  };

  const open = !!node || !!edge;

  return (
    <Drawer
      variant="permanent"
      anchor="right"
      sx={{
        width: WIDTH,
        flexShrink: 0,
        "& .MuiDrawer-paper": { width: WIDTH, boxSizing: "border-box", p: 2 },
      }}
      open
    >
      {!open ? (
        <Box sx={{ opacity: 0.6, mt: 6, textAlign: "center" }}>
          <Typography>Select a status or a transition to edit</Typography>
        </Box>
      ) : (
        <Box>
          <Stack
            direction="row"
            justifyContent="space-between"
            alignItems="center"
          >
            <Typography variant="h6">
              {node ? "Status details" : "Transition details"}
            </Typography>
            <Typography
              onClick={close}
              sx={{ cursor: "pointer", fontWeight: 600, opacity: 0.6 }}
            >
              Close
            </Typography>
          </Stack>

          <Divider sx={{ my: 2 }} />

          {/* Status editor */}
          {node && (
            <Box>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>
                Status name
              </Typography>
              <TextField
                fullWidth
                value={String((node.data as any)?.label ?? "")}
                onChange={(e) => renameStatus(selectedNodeId!, e.target.value)}
              />
            </Box>
          )}

          {/* Transition editor */}
          {edge && (
            <Box>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>
                Transition name
              </Typography>
              <TextField
                fullWidth
                value={String(edge.label ?? "")}
                onChange={(e) => renameTransition(edge.id, e.target.value)}
              />

              <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
                Guard (condition)
              </Typography>
              <TextField
                fullWidth
                multiline
                minRows={2}
                value={guard}
                onChange={(e) => setTransitionGuard(edge.id, e.target.value)}
                placeholder={`e.g. amount > 1000 && role === 'QA'`}
              />

              <Divider sx={{ my: 2 }} />

              <Tabs
                value={tab}
                onChange={(_, v) => setTab(v)}
                variant="fullWidth"
                textColor="primary"
                indicatorColor="primary"
              >
                <Tab label="Before" />
                <Tab label="During" />
                <Tab label="After" />
              </Tabs>

              <Box sx={{ mt: 2 }}>
                {tab === 0 && (
                  <TextField
                    label="Before"
                    fullWidth
                    multiline
                    minRows={4}
                    value={phases.before || ""}
                    onChange={(e) =>
                      setTransitionPhase(edge.id, "before", e.target.value)
                    }
                  />
                )}
                {tab === 1 && (
                  <TextField
                    label="During"
                    fullWidth
                    multiline
                    minRows={4}
                    value={phases.during || ""}
                    onChange={(e) =>
                      setTransitionPhase(edge.id, "during", e.target.value)
                    }
                  />
                )}
                {tab === 2 && (
                  <TextField
                    label="After"
                    fullWidth
                    multiline
                    minRows={4}
                    value={phases.after || ""}
                    onChange={(e) =>
                      setTransitionPhase(edge.id, "after", e.target.value)
                    }
                  />
                )}
              </Box>
            </Box>
          )}
        </Box>
      )}
    </Drawer>
  );
}
