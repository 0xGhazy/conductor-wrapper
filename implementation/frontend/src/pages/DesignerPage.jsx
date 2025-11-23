import { React, useCallback, useMemo, useRef, useState, useEffect } from "react";
import { Button, Input, Label, Card, CardContent, CardHeader, CardTitle } from "../Components/Inputs";
import { Link as LinkIcon, Database as DatabaseIcon, Braces as BracesIcon, Play as PlayIcon, Flag, Power, GitBranch, Shield, FileText, Clock, AlertCircle, X, Upload, Terminal, Cloud } from "lucide-react";
import {initialNodes, userDefinedActions} from "../Components/Fillers/FillerData";
import { ReactFlow, Background, Controls, MiniMap, addEdge, useEdgesState, useNodesState, Handle, Position, Panel} from "@xyflow/react";
import { HttpActionNode, HttpActionProps } from "../Components/Actions/http/HttpActionNode";
import { DatabaseActionProps, DatabaseActionNode } from "../Components/Actions/database/DatabaseActionNode";
import { RegexActionNode, RegexActionProps } from "../Components/Actions/regex/RegexActionNode";
import { TerraformActionNode, TerraformActionProps } from "../Components/Actions/terraform/TerraformActionNode"
import { JexlActionNode, JexlActionProps } from "../Components/Actions/jexl/JexlActionNode";

import { FlagNode } from "../Components/flow/FlagNode";
import TerminateActionProps from "../Components/PanelProps/TerminateActionProps";
import BranchActionProps from "../Components/PanelProps/BranchActionProps";
import LoggerProps from "../Components/PanelProps/LoggerProps";
import DelayProps from "../Components/PanelProps/DelayProps";
import ErrorHandlerProps from "../Components/PanelProps/ErrorHandlerProps";
import GenericUserActionProps from "../Components/PanelProps/GenericUserActionProps";


/** @typedef {"terraformAction"|"httpAction"|"dbAction"|"jexl"|"terminateAction"|"branchAction"|"regexValidator"|"logger"|"delay"|"errorHandler"} NodeKind */
const DRAG_TYPE = "application/x-node-type";

// Unified Panel Container (VS Code style with tabs)
function PanelContainer({ 
  isVisible, 
  activeTab, 
  onTabChange, 
  onClose,
  terminalProps,
  logsProps
}) {
  if (!isVisible) return null;

  return (
    <div className="absolute bottom-0 left-0 right-0 bg-[#1e1e1e] text-[#cccccc] border-t border-[#3e3e42] z-50"
         style={{ height: '350px', display: 'flex', flexDirection: 'column' }}>
      {/* Tab Bar */}
      <div className="flex items-center justify-between bg-[#252526] border-b border-[#3e3e42]">
        <div className="flex items-center">
          <button
            onClick={() => onTabChange('terminal')}
            className={`px-4 py-2 text-xs font-medium border-b-2 transition-colors ${
              activeTab === 'terminal'
                ? 'border-[#007acc] text-[#cccccc] bg-[#1e1e1e]'
                : 'border-transparent text-[#858585] hover:text-[#cccccc] hover:bg-[#2d2d30]'
            }`}
          >
            <div className="flex items-center gap-2">
              <Terminal size={14} />
              <span>Terminal</span>
            </div>
          </button>
          <button
            onClick={() => onTabChange('logs')}
            className={`px-4 py-2 text-xs font-medium border-b-2 transition-colors ${
              activeTab === 'logs'
                ? 'border-[#007acc] text-[#cccccc] bg-[#1e1e1e]'
                : 'border-transparent text-[#858585] hover:text-[#cccccc] hover:bg-[#2d2d30]'
            }`}
          >
            <div className="flex items-center gap-2">
              <FileText size={14} />
              <span>Logs</span>
              {logsProps.logs.length > 0 && (
                <span className="text-[#858585]">({logsProps.logs.length})</span>
              )}
            </div>
          </button>
        </div>
        <div className="flex items-center gap-2 px-4">
          {activeTab === 'terminal' && (
            <Button
              onClick={terminalProps.onClear}
              variant="secondary"
              style={{ 
                padding: '4px 8px', 
                fontSize: '12px',
                background: '#3c3c3c',
                color: '#cccccc',
                border: '1px solid #3e3e42'
              }}
            >
              Clear
            </Button>
          )}
          {activeTab === 'logs' && (
            <Button
              onClick={logsProps.onClear}
              variant="secondary"
              style={{ 
                padding: '4px 8px', 
                fontSize: '12px',
                background: '#3c3c3c',
                color: '#cccccc',
                border: '1px solid #3e3e42'
              }}
            >
              Clear
            </Button>
          )}
          <Button
            onClick={onClose}
            variant="secondary"
            style={{ 
              padding: '4px 8px',
              background: 'transparent',
              border: 'none',
              color: '#cccccc'
            }}
          >
            <X size={16} />
          </Button>
        </div>
      </div>

      {/* Tab Content */}
      <div className="flex-1 overflow-hidden" style={{ display: 'flex', flexDirection: 'column' }}>
        {activeTab === 'terminal' && (
          <TerminalContent {...terminalProps} />
        )}
        {activeTab === 'logs' && (
          <LogsContent {...logsProps} />
        )}
      </div>
    </div>
  );
}

// Terminal Content Component
function TerminalContent({ 
  output, 
  input, 
  onInputChange, 
  onExecute, 
  onClear,
  inputRef,
  containerRef
}) {
  const outputEndRef = useRef(null);

  useEffect(() => {
    // Auto-scroll to bottom when new output is added
    if (containerRef?.current && outputEndRef.current) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight;
    }
  }, [output, containerRef]);

  useEffect(() => {
    // Focus input when terminal tab is active
    if (inputRef?.current) {
      setTimeout(() => inputRef.current?.focus(), 100);
    }
  }, [inputRef]);

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      onExecute();
    }
  };

  return (
    <>
      {/* Terminal Output */}
      <div 
        ref={containerRef}
        className="flex-1 overflow-y-auto px-4 py-2 font-mono text-xs" 
        style={{ backgroundColor: '#1e1e1e' }}
      >
        {output.map((line, index) => {
          // Handle multiline output
          const lines = line.text.split('\n');
          return lines.map((textLine, lineIndex) => (
            <div 
              key={`${index}-${lineIndex}`}
              className="mb-0.5 whitespace-pre-wrap"
              style={{ 
                color: line.type === 'error' ? '#f87171' : 
                       line.type === 'command' ? '#60a5fa' : 
                       '#cccccc'
              }}
            >
              {line.type === 'command' && lineIndex === 0 && (
                <span className="text-[#858585]">$ </span>
              )}
              {textLine || '\u00A0'} {/* Non-breaking space for empty lines */}
            </div>
          ));
        })}
        <div ref={outputEndRef} />
      </div>

      {/* Terminal Input */}
      <div className="px-4 py-2 bg-[#252526] border-t border-[#3e3e42] flex items-center gap-2">
        <span className="text-[#858585] font-mono text-xs">$</span>
        <input
          ref={inputRef}
          type="text"
          value={input}
          onChange={(e) => onInputChange(e.target.value)}
          onKeyDown={handleKeyDown}
          className="flex-1 bg-transparent text-[#cccccc] font-mono text-xs outline-none border-none"
          placeholder="Enter command..."
        />
      </div>
    </>
  );
}

// Logs Content Component
function LogsContent({ logs }) {
  const logsContainerRef = useRef(null);

  useEffect(() => {
    // Auto-scroll to bottom when new logs are added
    if (logsContainerRef.current) {
      logsContainerRef.current.scrollTop = logsContainerRef.current.scrollHeight;
    }
  }, [logs]);

  const getLogLevelColor = (level) => {
    switch (level?.toUpperCase()) {
      case 'ERROR': return '#f87171'; // red
      case 'WARN': return '#fbbf24'; // yellow/amber
      case 'INFO': return '#60a5fa'; // blue
      case 'DEBUG': return '#a78bfa'; // purple
      default: return '#9ca3af'; // gray
    }
  };

  return (
    <div 
      ref={logsContainerRef}
      className="flex-1 overflow-y-auto px-4 py-2 font-mono text-xs" 
      style={{ backgroundColor: '#1e1e1e' }}
    >
      {logs.length === 0 ? (
        <div className="text-[#858585] italic">No logs yet. Logs will appear here when logger actions execute.</div>
      ) : (
        logs.map((log, index) => (
          <div key={index} className="mb-1" style={{ color: getLogLevelColor(log.level) }}>
            <span className="text-[#858585]">
              [{new Date(log.timestamp).toLocaleTimeString()}]
            </span>
            <span className="ml-2 font-semibold" style={{ color: getLogLevelColor(log.level) }}>
              [{log.level}]
            </span>
            <span className="ml-2 text-[#cccccc]">
              {log.nodeName}:
            </span>
            <span className="ml-2 text-[#cccccc]">
              {log.message}
            </span>
            {log.data && (
              <div className="ml-8 mt-1 text-[#858585]">
                {JSON.stringify(log.data, null, 2)}
              </div>
            )}
          </div>
        ))
      )}
    </div>
  );
}

function PropertiesPanel({ selectedNode, onChange, onDelete, userDefinedActions = [] }) {
  const noSelectedNodes = (!selectedNode || !selectedNode.data || selectedNode.data.name === undefined);
  if (noSelectedNodes) {
    return (
      <Card className="h-full">
        <CardHeader><CardTitle>Properties</CardTitle></CardHeader>
        <CardContent className="text-sm text-gray-500">
          
        <div>
            <Label>Workflow</Label>
            <Input
              value=""
              onChange={(e) => onField({ workflowName: e.target.value })}
            />
          </div>

          <div>
            <Label>Description</Label>
            <Input
              value=""
              onChange={(e) => onField({ workflowDescription: e.target.value })}
            />
          </div>

          <div>
            <Label>Version</Label>
            <Input
              value=""
              onChange={(e) => onField({ version: e.target.value })}
            />
          </div>

          <div>
            <Label>Last Updated At</Label>
            <Input
              value=""
              onChange={(e) => onField({ lastUpdatedAt: e.target.value })}
            />
          </div>

        </CardContent>
      </Card>
    );
  }

  const { data } = selectedNode;
  const Panel = PANEL_REGISTRY[data.kind] || PANEL_REGISTRY["*"];
  const onField = (patch) => onChange(patch);

  return (
    <Card className="h-full">
      <CardContent>
        <div className="space-y-4" style={{ maxHeight: "70vh", overflow: "auto", paddingRight: 6 }}>

          <div>
            <Label>Name</Label>
            <Input
              value={data.name || ""}
              onChange={(e) => onField({ name: e.target.value })}
            />
          </div>

          <Panel data={data} onChange={onField} userDefinedActions={userDefinedActions} />
        </div>
      </CardContent>
    </Card>
  );
}

const PANEL_REGISTRY = {
  httpAction: HttpActionProps,
  dbAction: DatabaseActionProps,
  regexValidator: RegexActionProps,
  terraformAction: TerraformActionProps,
  jexl: JexlActionProps,
  terminateAction: TerminateActionProps,
  branchAction: BranchActionProps,
  logger: LoggerProps,
  delay: DelayProps,
  errorHandler: ErrorHandlerProps,
  "*": GenericUserActionProps,
};

const TerminateActionNode = ({ data }) => {
  return (
    <div className="relative" style={{ pointerEvents: "all" }}>
      {/* Hidden target handle - still allows connections but not visible */}
      <Handle
        type="target"
        position={Position.Top}
        style={{ opacity: 0, pointerEvents: "auto" }}
      />
      <div className="rounded-2xl border bg-white min-w-[220px]">
        <div
          className="flex items-center gap-2 px-3 py-2 border-b"
          style={{ borderColor: "#f59e0b" }}
        >
          <div className="p-1 rounded-lg" style={{ background: "#f59e0b20" }}>
            <Power size={16} />
          </div>
          <span className="font-medium text-sm">
            {data?.name || "Terminate"}
          </span>
        </div>
        <div className="px-3 py-2 text-xs text-gray-600 truncate max-w-[220px]">
          {data?.config?.reason || "Workflow terminated"}
        </div>
      </div>
    </div>
  );
};

const BranchActionNode = ({ data }) => {
  return (
    <div className="relative" style={{ pointerEvents: "all" }}>
      <Handle
        type="target"
        position={Position.Top}
        style={{ opacity: 0, pointerEvents: "auto" }}
      />
      <div className="rounded-2xl border bg-white min-w-[220px]">
        <div
          className="flex items-center gap-2 px-3 py-2 border-b"
          style={{ borderColor: "#8b5cf6" }}
        >
          <div className="p-1 rounded-lg" style={{ background: "#8b5cf620" }}>
            <GitBranch size={16} />
          </div>
          <span className="font-medium text-sm">{data?.name || "Branch"}</span>
        </div>
        <div className="px-3 py-2 text-xs text-gray-600 truncate max-w-[220px]">
          {data?.config?.condition || "condition"}
        </div>
      </div>
      <div className="relative pb-4">
        <Handle
          type="source"
          position={Position.Bottom}
          id="true"
          style={{ left: "25%", bottom: "-8px" }}
          nonce="true"
          label="True"
        />
        <Handle
          type="source"
          position={Position.Bottom}
          id="false"
          style={{ right: "25%", bottom: "-8px" }}
          nonce="false"
          label="False"
        />
      </div>
    </div>
  );
};

const DelayNode = ({ data }) => {
  return (
    <div className="relative" style={{ pointerEvents: "all" }}>
      <Handle type="target" position={Position.Top} />
      <div className="rounded-2xl border bg-white min-w-[220px]">
        <div
          className="flex items-center gap-2 px-3 py-2 border-b"
          style={{ borderColor: "#8b5cf6" }}
        >
          <div className="p-1 rounded-lg" style={{ background: "#8b5cf620" }}>
            <Clock size={16} />
          </div>
          <span className="font-medium text-sm">
            Delay: {data?.config?.duration || 0}ms
          </span>
        </div>
      </div>
      <div className="relative pb-4">
        <Handle
          type="source"
          position={Position.Bottom}
          id="success"
          style={{ right: "25%", bottom: "-8px" }}
        />
      </div>
    </div>
  );
};

const LoggerNode = ({ data }) => (
  <ActionNode
    data={data}
    icon={<FileText size={16} />}
    accent="#6b7280"
    defaultTitle="Logger"
    targetHandlePosition={Position.Top}
    sourceHandlePosition={Position.Bottom}
  />
);

const ErrorHandlerNode = ({ data }) => {
  return (
    <div className="relative" style={{ pointerEvents: "all" }}>
      <Handle type="target" position={Position.Top} />
      <div className="rounded-2xl border bg-white min-w-[220px]">
        <div
          className="flex items-center gap-2 px-3 py-2 border-b"
          style={{ borderColor: "#ef4444" }}
        >
          <div className="p-1 rounded-lg" style={{ background: "#ef444420" }}>
            <AlertCircle size={16} />
          </div>
          <span className="font-medium text-sm">
            {data?.name || "Error Handler"}
          </span>
        </div>
        <div className="px-3 py-2 text-xs text-gray-600 truncate max-w-[220px]">
          {data?.config?.errorType || "Any error"}
        </div>
      </div>
      <div className="relative pb-4">
        <Handle
          type="source"
          position={Position.Bottom}
          id="success"
          style={{ left: "25%", bottom: "-8px" }}
        />
        <Handle
          type="source"
          position={Position.Bottom}
          id="error"
          style={{ right: "25%", bottom: "-8px" }}
        />
        <div className="absolute left-[15%] -bottom-2 text-[10px] text-gray-500 font-medium">
          Success
        </div>
        <div className="absolute right-[15%] -bottom-2 text-[10px] text-gray-500 font-medium">
          Error
        </div>
      </div>
    </div>
  );
};

// Generic User Defined Action Node Component
const UserDefinedActionNode = ({ data }) => {
  const actionConfig = userDefinedActions.find((a) => a.type === data?.kind);

  if (!actionConfig) {
    return null;
  }

  return (
    <ActionNode
      data={data}
      icon={actionConfig.icon}
      accent={actionConfig.accent}
      defaultTitle={actionConfig.name}
      targetHandlePosition={Position.Top}
      sourceHandlePosition={Position.Bottom}
    />
  );
};

const StartFlagNode = () => (
  <FlagNode
    color="#10b981"
    icon={<Flag size={16} />}
    text="Start"
    handleType="source"
    handlePosition={Position.Bottom}
  />
);

function PaletteItem({ kind, label, icon }) {
  const onDragStart = (e) => {
    e.dataTransfer.setData(DRAG_TYPE, kind);
    e.dataTransfer.effectAllowed = "move";
  };
  return (
    <Button
      variant="secondary"
      draggable
      onDragStart={onDragStart}
      style={{ display: "flex", alignItems: "center", gap: 8, width: "100%" }}
    >
      {icon}
      {label}
    </Button>
  );
}


export default function DesignerPage() {
    const searchInputRef = useRef(null);
    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChangeBase] = useEdgesState([]);
    const [selectedId, setSelectedId] = useState(null);
    const flowRef = useRef(null);
    const [userActionSearch, setUserActionSearch] = useState("");
    const [showAutocomplete, setShowAutocomplete] = useState(false);
    const fileInputRef = useRef(null);
    const [logs, setLogs] = useState([]);
    const [showPanel, setShowPanel] = useState(false);
    const [activePanelTab, setActivePanelTab] = useState('terminal');
    const [terminalHistory, setTerminalHistory] = useState([]);
    const [terminalInput, setTerminalInput] = useState("");
    const [terminalHistoryIndex, setTerminalHistoryIndex] = useState(-1);
    const [terminalOutput, setTerminalOutput] = useState([{ type: 'output', text: 'Terminal ready. Connect to backend to execute commands.' }]);
    const terminalInputRef = useRef(null);
    const terminalContainerRef = useRef(null);

  const filteredUserActions = useMemo(() => {
    if (!userActionSearch.trim()) {
      return userDefinedActions;
    }
    const searchLower = userActionSearch.toLowerCase();
    return userDefinedActions.filter((action) =>
      action.name.toLowerCase().includes(searchLower)
    );
  }, [userActionSearch]);

  // Helper function to check if a node would be orphaned (no incoming edges)
  const wouldBeOrphaned = useCallback((nodeId, currentEdges) => {
    // Start node can not be orphaned (it's the root)
    if (nodeId === "start") return false;
    
    // Check if node has any incoming edges
    return !currentEdges.some(edge => edge.target === nodeId);
  }, []);

  // Wrapped onEdgesChange to prevent orphan nodes
  const onEdgesChange = useCallback((changes) => {
    // Filter out changes that would create orphan nodes
    const validChanges = changes.filter((change) => {
      // Only validate edge removals
      if (change.type === "remove") {
        const edgeToRemove = edges.find(e => e.id === change.id);
        if (edgeToRemove) {
          // Check if removing this edge would orphan the target node
          const remainingEdges = edges.filter(e => e.id !== change.id);
          
          if (wouldBeOrphaned(edgeToRemove.target, remainingEdges)) {
            // Prevent the deletion and show warning
            const targetNode = nodes.find(n => n.id === edgeToRemove.target);
            const nodeName = targetNode?.data?.name || targetNode?.id || "node";
            alert(
              `Cannot remove this connection. The node "${nodeName}" would be left without a parent connection.\n\n` +
              `Every node (except the start node) must have at least one incoming edge.`
            );
            return false; // Filter out this change
          }
        }
      }
      return true; // Allow all other changes
    });
    
    // Apply the filtered changes
    if (validChanges.length > 0) {
      onEdgesChangeBase(validChanges);
    }
  }, [edges, nodes, wouldBeOrphaned, onEdgesChangeBase]);

  // Build dynamic node types including user-defined actions
  const allNodeTypes = useMemo(() => {
    const baseTypes = {
      httpAction: HttpActionNode,
      dbAction: DatabaseActionNode,
      jexl: JexlActionNode,
      terminateAction: TerminateActionNode,
      terraformAction: TerraformActionNode,
      branchAction: BranchActionNode,
      regexValidator: RegexActionNode,
      logger: LoggerNode,
      delay: DelayNode,
      errorHandler: ErrorHandlerNode,
      startFlag: StartFlagNode,
    };

    // Add user-defined action types dynamically
    const userTypes = {};
    userDefinedActions.forEach((action) => {
      userTypes[action.type] = UserDefinedActionNode;
    });

    return { ...baseTypes, ...userTypes };
  }, []);



  // Autocomplete suggestions
  const autocompleteSuggestions = useMemo(() => {
    if (!userActionSearch.trim() || !showAutocomplete) {
      return [];
    }
    const searchLower = userActionSearch.toLowerCase();
    return userDefinedActions
      .filter((action) => action.name.toLowerCase().includes(searchLower))
  }, [userActionSearch, showAutocomplete]);

  // Handle autocomplete selection
  const handleAutocompleteSelect = useCallback((action) => {
    setUserActionSearch(action.name);
    setShowAutocomplete(false);
    // Auto-close after a delay to show the filtered result
    setTimeout(() => {
      setUserActionSearch("");
    }, 1500);
  }, []);

  // Handle clicking outside to close autocomplete
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        searchInputRef.current &&
        !searchInputRef.current.contains(event.target)
      ) {
        setShowAutocomplete(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Apply colors to branch and error handler action edges
  const styledEdges = useMemo(() => {
    return edges.map((edge) => {
      // Color edges from branch action handles (green for true, red for false)
      if (edge.sourceHandle === "true") {
        return {
          ...edge,
          style: { stroke: "#22c55e", strokeWidth: 2 },
        };
      } else if (edge.sourceHandle === "false") {
        return {
          ...edge,
          style: { stroke: "#ef4444", strokeWidth: 2 },
        };
      }
      // Color edges from error handler handles (green for success, red for error)
      else if (edge.sourceHandle === "success") {
        return {
          ...edge,
          style: { stroke: "#22c55e", strokeWidth: 2 },
        };
      } else if (edge.sourceHandle === "error") {
        return {
          ...edge,
          style: { stroke: "#ef4444", strokeWidth: 2 },
        };
      }
      return edge;
    });
  }, [edges]);

  const isValidConnection = useCallback(
    (connection) => {
      // Prevent self-loops (node connecting to itself)
      if (connection.source === connection.target) return false;

      // Prevent connection to start node
      if (connection.target === "start") return false;
      // Prevent connection from end node
      if (connection.source === "end") return false;
      // Prevent multiple connections from start node
      if (connection.source === "start") {
        const hasStartEdge = edges.some((edge) => edge.source === "start");
        return !hasStartEdge;
      }

      // Prevent connections from terminateAction (it's a terminal node)
      const sourceNode = nodes.find((n) => n.id === connection.source);
      if (sourceNode && sourceNode.type === "terminateAction") {
        return false;
      }

      return true;
    },
    [edges, nodes]
  );

  const onConnect = useCallback(
    (connection) => {
      // Prevent self-loops (node connecting to itself)
      if (connection.source === connection.target) return;

      // Prevent connection from END or from START
      if (connection.target === "start") return;
      if (connection.source === "end") return;

      // Prevent connections from terminateAction (it's a terminal node)
      const sourceNode = nodes.find((n) => n.id === connection.source);
      if (sourceNode && sourceNode.type === "terminateAction") return;

      // Prevent multiple connections from start node
      if (connection.source === "start") {
        setEdges((eds) => {
          // Check if there's already an edge from "start"
          const hasStartEdge = eds.some((edge) => edge.source === "start");
          if (hasStartEdge) {
            // Remove existing start edge and add new one
            return eds
              .filter((edge) => edge.source !== "start")
              .concat(addEdge({ ...connection }, []));
          }
          return addEdge({ ...connection }, eds);
        });
        return;
      }

      // Add color styling for branch and error handler action edges
      const edgeData = { ...connection };

      // Color edges from branch action handles (green for true, red for false)
      if (connection.sourceHandle === "true") {
        edgeData.style = { stroke: "#22c55e", strokeWidth: 2 };
        edgeData.animated = false;
      } else if (connection.sourceHandle === "false") {
        edgeData.style = { stroke: "#ef4444", strokeWidth: 2 };
        edgeData.animated = false;
      }
      // Color edges from error handler handles (green for success, red for error)
      else if (connection.sourceHandle === "success") {
        edgeData.style = { stroke: "#22c55e", strokeWidth: 2 };
        edgeData.animated = false;
      } else if (connection.sourceHandle === "error") {
        edgeData.style = { stroke: "#ef4444", strokeWidth: 2 };
        edgeData.animated = false;
      }

      setEdges((eds) => addEdge(edgeData, eds));
    },
    [nodes]
  );

  const onDragOver = useCallback((e) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = "move";
  }, []);

  const onDrop = useCallback((e) => {
    e.preventDefault();
    const kind = e.dataTransfer.getData(DRAG_TYPE);
    if (!kind) return;

    const bounds =
      flowRef.current?.getBoundingClientRect?.() ??
      document.body.getBoundingClientRect();
    const pos = {
      x: e.clientX - bounds.left - 200,
      y: e.clientY - bounds.top - 40,
    };

    const id = `${kind}_${Date.now()}`;
    let data;

    if (kind === "httpAction")
      data = {
        kind: "httpAction",
        name: "HTTP",
        config: { method: "GET", url: "", headers: "{}", query: "{}", body: "{}", timeout: 30, connection: "--NONE--" },
    };
    else if (kind === "dbAction")
      data = {
        kind: "dbAction",
        name: "Query",
        config: { query: "getCustomerBalance1"},
      };
    else if (kind === "terminateAction")
      data = {
        kind: "terminateAction",
        name: "Terminate",
        config: { reason: "Workflow terminated" },
      };
    else if (kind === "branchAction")
      data = {
        kind: "branchAction",
        name: "Branch",
        config: { condition: "input.value > 0" },
      };
    else if (kind === "regexValidator")
      data = {
        kind: "regexValidator",
        name: "Regex Validator",
        config: { pattern: "/^[a-zA-Z0-9]+$/", field: "input.value" },
      };
    else if (kind === "logger")
      data = {
        kind: "logger",
        name: "Logger",
        config: { level: "INFO", message: "Log message" },
      };
    else if (kind === "delay")
      data = {
        kind: "delay",
        name: "Delay",
        config: { duration: 1000 },
      };
    else if (kind === "errorHandler")
      data = {
        kind: "errorHandler",
        name: "Error Handler",
        config: { errorType: "Any error", fallbackValue: null },
      };
    else if (kind === "terraformAction")
      data = {
        name: "Terraform",
        config: { action: "Create new ES2 Instance", name: "Terraform" },
      };
    else {
      // Check if it's a user-defined action
      const userAction = userDefinedActions.find((a) => a.type === kind);
      if (userAction) {
        data = {
          kind: userAction.type,
          name: userAction.name,
          config: { ...userAction.defaultConfig },
        };
      }
    }

    setNodes((nds) => nds.concat({ id, type: kind, position: pos, data, selectable: true }));
    setSelectedId(id);
  }, []);

  const selectedNode = useMemo(() => {
    const node = nodes.find((n) => n.id === selectedId) || null;
    // console.log('selectedNode computed:', { selectedId, found: !!node, nodeData: node?.data });
    return node;
  }, [nodes, selectedId]);

  const handleChange = useCallback(
    (patch) => {
      if (!selectedNode) return;
      setNodes((nds) =>
        nds.map((n) =>
          n.id === selectedNode.id
            ? {
                ...n,
                data: {
                  ...n.data,
                  ...patch,
                  config: { ...n.data.config, ...(patch?.config || {}) },
                },
              }
            : n
        )
      );
    },
    [selectedNode, setNodes]
  );

  // Handle node click with useCallback to prevent re-renders
  const handleNodeClick = useCallback((event, node) => {
    event.stopPropagation();
    setSelectedId(node.id);
  }, [selectedId]);

  const handleDelete = useCallback(() => {
    if (!selectedNode) return;
    const id = selectedNode.id;
    if (id === "start" || id === "end") return;
    
    // Find all outgoing edges from this node
    const outgoingEdges = edges.filter(e => e.source === id);
    
    // Check if deleting this node would orphan any child nodes
    const wouldOrphanNodes = outgoingEdges
      .map(edge => {
        const remainingEdges = edges.filter(e => 
          e.id !== edge.id && e.target === edge.target
        );
        return {
          edge,
          targetNode: nodes.find(n => n.id === edge.target),
          wouldBeOrphaned: wouldBeOrphaned(edge.target, remainingEdges)
        };
      })
      .filter(result => result.wouldBeOrphaned);
    
    if (wouldOrphanNodes.length > 0) {
      const orphanedNodeNames = wouldOrphanNodes
        .map(r => r.targetNode?.data?.name || r.targetNode?.id || "node")
        .join(", ");
      
      alert(
        `Cannot delete "${selectedNode.data?.name || id}". This would leave the following node(s) without a parent connection:\n\n` +
        `${orphanedNodeNames}\n\n` +
        `Please reconnect or delete those nodes first, or ensure they have other parent connections.`
      );
      return;
    }
    
    // Safe to delete - remove node and all its edges
    setNodes((nds) => nds.filter((n) => n.id !== id));
    setEdges((eds) => eds.filter((e) => e.source !== id && e.target !== id));
    setSelectedId(null);
  }, [selectedNode, edges, nodes, wouldBeOrphaned]);

  // Add log entry (called when logger actions execute)
  const addLog = useCallback((level, nodeName, message, data = null) => {
    setLogs(prev => [...prev, {
      timestamp: Date.now(),
      level,
      nodeName,
      message,
      data
    }]);
  }, []);

  // Clear logs
  const clearLogs = useCallback(() => {
    setLogs([]);
  }, []);

  // Execute terminal command
  const executeTerminalCommand = useCallback(async (command) => {
    if (!command.trim()) return;

    // Add command to output
    setTerminalOutput(prev => [...prev, { type: 'command', text: command }]);
    
    // Add to history
    setTerminalHistory(prev => [...prev, command]);
    setTerminalHistoryIndex(-1);
    setTerminalInput("");

    // TODO: Connect to backend API endpoint to execute commands
    // For now, simulate command execution
    // Replace this with actual API call when backend is ready
    
    // Simulate command execution with a delay
    setTimeout(() => {
      // For now, just show a message that backend connection is needed
      // In production, this would be:
      // const response = await fetch('/api/terminal/execute', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify({ command })
      // });
      // const result = await response.json();
      
      const isError = Math.random() > 0.7; // Simulate occasional errors
      
      if (isError) {
        setTerminalOutput(prev => [...prev, {
          type: 'error',
          text: `Error: Command execution requires backend connection. Configure API endpoint in settings.`
        }]);
      } else {
        // Simulate output based on common commands
        let output = '';
        if (command.trim() === 'help') {
          output = `Available commands:\n  help - Show this help message\n  clear - Clear terminal output\n  echo <text> - Echo text back\n\nNote: Full command execution requires backend API connection.`;
        } else if (command.trim().startsWith('echo ')) {
          output = command.replace('echo ', '');
        } else if (command.trim() === 'clear') {
          setTerminalOutput([]);
          return;
        } else {
          output = `Command "${command}" - Backend connection required for execution.\nConfigure terminal API endpoint to enable command execution.`;
        }
        
        setTerminalOutput(prev => [...prev, {
          type: 'output',
          text: output
        }]);
      }
    }, 300);
  }, []);

  // Clear terminal
  const clearTerminal = useCallback(() => {
    setTerminalOutput([{ type: 'output', text: 'Terminal cleared.' }]);
  }, []);

  // Handle terminal input history navigation (arrow keys)
  const handleTerminalHistoryNavigation = useCallback((direction) => {
    if (terminalHistory.length === 0) return;
    
    let newIndex = terminalHistoryIndex;
    if (direction === 'up') {
      newIndex = newIndex === -1 ? terminalHistory.length - 1 : Math.max(0, newIndex - 1);
    } else if (direction === 'down') {
      newIndex = newIndex === -1 ? -1 : Math.min(terminalHistory.length - 1, newIndex + 1);
    }
    
    setTerminalHistoryIndex(newIndex);
    if (newIndex === -1) {
      setTerminalInput("");
    } else {
      setTerminalInput(terminalHistory[newIndex]);
    }
  }, [terminalHistory, terminalHistoryIndex]);

  // Keyboard shortcuts
  useEffect(() => {
    const onKeyDown = (e) => {
      const el = e.target;
      const tag = el?.tagName;
      const inEditable =
        tag === "INPUT" || tag === "TEXTAREA" || el?.isContentEditable;

      // CTRL + ` (backtick) to toggle panel
      if (e.ctrlKey && e.key === "`") {
        e.preventDefault();
        setShowPanel(prev => {
          if (!prev) {
            // If opening, default to terminal tab
            setActivePanelTab('terminal');
          }
          return !prev;
        });
        return;
      }

      // CTRL + SHIFT + ` (backtick) to toggle between terminal and logs tabs
      if (e.ctrlKey && e.shiftKey && e.key === "`") {
        e.preventDefault();
        if (showPanel) {
          setActivePanelTab(prev => prev === 'terminal' ? 'logs' : 'terminal');
        } else {
          setShowPanel(true);
          setActivePanelTab('logs');
        }
        return;
      }

      // Terminal history navigation (only when terminal tab is active)
      if (showPanel && activePanelTab === 'terminal' && !inEditable) {
        if (e.key === "ArrowUp") {
          e.preventDefault();
          handleTerminalHistoryNavigation('up');
          return;
        }
        if (e.key === "ArrowDown") {
          e.preventDefault();
          handleTerminalHistoryNavigation('down');
          return;
        }
      }

      if (
        (e.key === "Delete" || e.key === "Backspace") &&
        selectedId &&
        !inEditable
      ) {
        e.preventDefault();
        handleDelete();
      }
    };

    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [selectedId, handleDelete, showPanel, activePanelTab, handleTerminalHistoryNavigation]);

  // const flowComplete = useMemo(
  //   () => edges.some((e) => e.target === "end"),
  //   [edges]
  // );

  // Validate that no nodes are orphaned (have no incoming edges, except start)
  const validateOrphanNodes = useCallback((nodesToCheck, edgesToCheck) => {
    const orphanNodes = nodesToCheck.filter(node => {
      // Start node can be orphaned (it's the root)
      if (node.id === "start") return false;
      
      // Check if node has any incoming edges
      return !edgesToCheck.some(edge => edge.target === node.id);
    });

    if (orphanNodes.length > 0) {
      const orphanNames = orphanNodes
        .map(n => n.data?.name || n.id)
        .join(", ");
      return {
        valid: false,
        message: `The following node(s) have no incoming connections:\n\n${orphanNames}\n\nEvery node (except the start node) must have at least one incoming edge. Please connect these nodes before proceeding.`,
      };
    }

    return { valid: true, message: null };
  }, []);

  const validateFlowHasDatabaseAction = useCallback(() => {
    // First check for orphan nodes
    const orphanValidation = validateOrphanNodes(nodes, edges);
    if (!orphanValidation.valid) {
      return orphanValidation;
    }

    // Check if start node exists
    const startNode = nodes.find((node) => node.id === "start");
    if (!startNode) {
      return {
        valid: false,
        message: "Start node not found",
      };
    }

    // Check if start node has any outgoing edges
    const hasStartConnection = edges.some((e) => e.source === "start");
    if (!hasStartConnection) {
      return {
        valid: false,
        message:
          "Flow must start from the start node. Connect the start node to an action.",
      };
    }

    // Build adjacency list from edges
    const graph = new Map();
    edges.forEach((edge) => {
      if (!graph.has(edge.source)) {
        graph.set(edge.source, []);
      }
      graph.get(edge.source).push(edge.target);
    });

    // Create a map of node types for quick lookup
    const nodeTypeMap = new Map();
    nodes.forEach((node) => {
      nodeTypeMap.set(node.id, node.type);
    });

    // DFS to check all paths from start end with a terminateAction
    const invalidPaths = [];
    const visitedInPath = new Set();

    const dfs = (currentNodeId, path) => {
      // Detect cycles to avoid infinite recursion
      if (visitedInPath.has(currentNodeId)) {
        return; // Cycle detected, skip
      }

      const currentNodeType = nodeTypeMap.get(currentNodeId);
      if (!currentNodeType) return;

      visitedInPath.add(currentNodeId);
      const newPath = [...path, currentNodeId];

      // Get all neighbors
      const neighbors = graph.get(currentNodeId) || [];

      // If no neighbors (path ended), check if it's a valid endpoint
      if (neighbors.length === 0) {
        // Path ended - must end with terminateAction to be valid
        if (currentNodeType !== "terminateAction") {
          invalidPaths.push(newPath);
        }
        visitedInPath.delete(currentNodeId);
        return;
      }

      // If current node is terminateAction, this path is valid - stop here
      if (currentNodeType === "terminateAction") {
        visitedInPath.delete(currentNodeId);
        return;
      }

      // Continue exploring all neighbors
      neighbors.forEach((neighborId) => {
        dfs(neighborId, newPath);
      });

      visitedInPath.delete(currentNodeId);
    };

    // Start DFS from start node
    dfs("start", []);

    if (invalidPaths.length > 0) {
      return {
        valid: false,
        message: `Some flow paths do not end with a Terminate action. Please ensure all paths from start end with a Terminate action.`,
      };
    }

    return {
      valid: true,
      message:
        "All flows are properly connected and end with Terminate actions.",
    };
  }, [nodes, edges, validateOrphanNodes]);


  const EXPORT_ENDPOINT = "http://localhost:9090/api/ui/run";
  
  async function sendExport(payload) {

    const signal = (AbortSignal && AbortSignal.timeout)
      ? AbortSignal.timeout(15000)
      : undefined;
  
    const res = await fetch(EXPORT_ENDPOINT, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      signal,
    });
  
    if (!res.ok) {
      const errText = await res.text().catch(() => "");
      throw new Error(`HTTP ${res.status}: ${errText || res.statusText}`);
    }
    try {
      return await res.json();
    } catch {
      return {};
    }
  }

  const handleRun = useCallback(async () => {
    const validation = validateFlowHasDatabaseAction();
    if (!validation.valid) {
      alert(validation.message);
      return;
    }
  
    clearLogs();
    const payload = buildExportPayload();
    try {
      addLog("INFO", "Client", "Exporting workflow…", payload.meta);
      const result = await sendExport(payload);
      addLog("INFO", "API", "Export OK", result);
      alert("Export sent successfully");
    } catch (err) {
      addLog("ERROR", "API", err && err.message ? err.message : "Export failed");
      alert(`Export failed: ${err && err.message ? err.message : "Unknown error"}`);
    }
  
    const loggerNodes = nodes.filter((n) => n.type === "logger");
    loggerNodes.forEach((loggerNode) => {
      const level = (loggerNode.data && loggerNode.data.config && loggerNode.data.config.level) || "INFO";
      const message = (loggerNode.data && loggerNode.data.config && loggerNode.data.config.message) || "No message";
      const nodeName = (loggerNode.data && loggerNode.data.name) || loggerNode.id;
      setTimeout(() => addLog(level, nodeName, message, loggerNode.data && loggerNode.data.config), Math.random() * 1000);
    });
  }, [validateFlowHasDatabaseAction, nodes, clearLogs, addLog]);


  // const handleRun = useCallback(() => {
  //   const validation = validateFlowHasDatabaseAction();
  //   if (!validation.valid) {
  //     alert(validation.message);
  //     return;
  //   }
    
  //   // Clear previous logs when running a new flow
  //   clearLogs();
    
  //   // Find all logger nodes in the workflow
  //   const loggerNodes = nodes.filter(n => n.type === "logger");
    
  //   // Simulate logger execution (when backend is connected, this will come from actual execution)
  //   loggerNodes.forEach((loggerNode) => {
  //     const level = loggerNode.data?.config?.level || "INFO";
  //     const message = loggerNode.data?.config?.message || "No message";
  //     const nodeName = loggerNode.data?.name || loggerNode.id;
      
  //     // Add log entry
  //     setTimeout(() => {
  //       addLog(level, nodeName, message, loggerNode.data?.config);
  //     }, Math.random() * 1000); // Simulate async execution
  //   });
    
  //   // Proceed with running the flow
  //   alert("Run flow – connect to backend API later");
  // }, [validateFlowHasDatabaseAction, nodes, clearLogs, addLog]);



  const buildGraph = (nodesArr, edgesArr) => {
    const out = new Map();
    const indeg = new Map();
    nodesArr.forEach(n => { out.set(n.id, []); indeg.set(n.id, 0); });
    edgesArr.forEach(e => {
      if (!out.has(e.source)) out.set(e.source, []);
      out.get(e.source).push({ to: e.target, handle: e.sourceHandle || null });
      indeg.set(e.target, (indeg.get(e.target) || 0) + 1);
    });
    return { out, indeg };
  };
  

  const enumeratePathsFromStart = (nodesArr, edgesArr, startId = "start") => {
    const { out } = buildGraph(nodesArr, edgesArr);
    const paths = [];
    const stack = [{ id: startId, path: [startId] }];
    const visiting = new Set();
  
    while (stack.length) {
      const { id, path } = stack.pop();
      if (visiting.has(id)) continue;
      visiting.add(id);
  
      const nexts = out.get(id) || [];
      if (nexts.length === 0) {
        paths.push(path.slice());
      } else {
        nexts.forEach(n => stack.push({ id: n.to, path: [...path, n.to] }));
      }
      visiting.delete(id);
    }
    return paths;
  };
  

  const topoFromStart = (nodesArr, edgesArr, startId = "start") => {
    const { out, indeg } = buildGraph(nodesArr, edgesArr);
    const reachable = new Set();
    const q0 = [startId];
    while (q0.length) {
      const u = q0.shift();
      if (reachable.has(u)) continue;
      reachable.add(u);
      (out.get(u) || []).forEach(v => q0.push(v.to));
    }
    // Kahn
    const indegR = new Map([...indeg].filter(([k]) => reachable.has(k)));
    const q = [];
    indegR.forEach((d, k) => { if (d === 0 && reachable.has(k)) q.push(k); });
    const order = [];
    while (q.length) {
      const u = q.shift();
      if (!reachable.has(u)) continue;
      order.push(u);
      (out.get(u) || []).forEach(({ to }) => {
        if (!reachable.has(to)) return;
        indegR.set(to, (indegR.get(to) || 0) - 1);
        if (indegR.get(to) === 0) q.push(to);
      });
    }
    return order;
  };

  const buildExportPayload = () => {
    const paths = enumeratePathsFromStart(nodes, edges);
    const order = topoFromStart(nodes, edges);
  
    // Build branch action routing information
    const branchActions = nodes
      .filter(n => n.type === "branchAction")
      .map(branchNode => {
        // Find edges connected to this branch node
        const connectedEdges = edges.filter(e => e.source === branchNode.id);
        
        // Find true and false paths
        const trueEdge = connectedEdges.find(e => e.sourceHandle === "true");
        const falseEdge = connectedEdges.find(e => e.sourceHandle === "false");
        
        // Get target nodes for true and false paths
        const truePath = trueEdge ? {
          targetNodeId: trueEdge.target,
          targetNodeName: nodes.find(n => n.id === trueEdge.target)?.data?.name || null,
          edgeId: trueEdge.id || `${trueEdge.source}->${trueEdge.target}`,
        } : null;
        
        const falsePath = falseEdge ? {
          targetNodeId: falseEdge.target,
          targetNodeName: nodes.find(n => n.id === falseEdge.target)?.data?.name || null,
          edgeId: falseEdge.id || `${falseEdge.source}->${falseEdge.target}`,
        } : null;
        
        return {
          branchNodeId: branchNode.id,
          branchNodeName: branchNode.data?.name || "Branch",
          condition: branchNode.data?.config?.condition || null,
          truePath,
          falsePath,
        };
      });
  
    return {
      meta: {
        workflowName: "Untitled",
        exportedAt: new Date().toISOString(),
        version: 1,
      },
      nodes: nodes.map(n => ({
        id: n.id,
        type: n.type,
        position: n.position,
        data: n.data,
      })),
      edges: edges.map(e => ({
        id: e.id || `${e.source}->${e.target}`,
        source: e.source,
        target: e.target,
        sourceHandle: e.sourceHandle || null,
        targetHandle: e.targetHandle || null,
      })),
      order,
      paths,
      branchActions,
    };
  };
  
  const downloadJSON = (obj, filename = "workflow.json") => {
    const blob = new Blob([JSON.stringify(obj, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  };
  
  const handleExport = () => {
    // Validate no orphan nodes before exporting
    const orphanValidation = validateOrphanNodes(nodes, edges);
    if (!orphanValidation.valid) {
      alert(orphanValidation.message);
      return;
    }
    
    const payload = buildExportPayload();
    downloadJSON(payload);
  };

  // Import workflow from JSON file
  const importFromJSON = useCallback((jsonData) => {
    try {
      // Validate the JSON structure
      if (!jsonData || typeof jsonData !== 'object') {
        throw new Error('Invalid JSON format');
      }

      if (!Array.isArray(jsonData.nodes) || !Array.isArray(jsonData.edges)) {
        throw new Error('JSON must contain nodes and edges arrays');
      }

      // Restore nodes - ensure they have all required properties
      const importedNodes = jsonData.nodes.map(node => {
        const nodeData = node.data || {};
        // Ensure kind is set - use data.kind if available, otherwise fall back to node.type
        const kind = nodeData.kind || node.type;
        
        return {
          id: node.id,
          type: node.type,
          position: node.position || { x: 0, y: 0 },
          data: {
            ...nodeData,
            kind: kind,
            name: nodeData.name || '',
            config: nodeData.config || {},
          },
          selectable: node.type === 'startFlag' ? false : true,
          draggable: node.type === 'startFlag' ? false : true,
        };
      });

      // Ensure start node exists or add it if missing
      const hasStartNode = importedNodes.some(n => n.id === 'start' && n.type === 'startFlag');
      const finalNodes = hasStartNode 
        ? importedNodes 
        : [...initialNodes, ...importedNodes.filter(n => n.id !== 'start')];

      // Restore edges
      const importedEdges = jsonData.edges.map(edge => ({
        id: edge.id || `${edge.source}->${edge.target}`,
        source: edge.source,
        target: edge.target,
        sourceHandle: edge.sourceHandle || null,
        targetHandle: edge.targetHandle || null,
        // Preserve edge styling if it was exported
        style: edge.style || undefined,
        animated: edge.animated || false,
      }));

      // Validate no orphan nodes in imported workflow
      const orphanValidation = validateOrphanNodes(finalNodes, importedEdges);
      if (!orphanValidation.valid) {
        alert(
          `Cannot import workflow with orphan nodes.\n\n${orphanValidation.message}\n\n` +
          `Please fix the workflow in the source file before importing.`
        );
        return false;
      }

      // Update state
      setNodes(finalNodes);
      setEdges(importedEdges);
      setSelectedId(null);

      // Show success message
      alert(`Workflow imported successfully!\nNodes: ${finalNodes.length}\nEdges: ${importedEdges.length}`);

      return true;
    } catch (error) {
      console.error('Import error:', error);
      alert(`Failed to import workflow: ${error.message}`);
      return false;
    }
  }, [setNodes, setEdges, validateOrphanNodes]);

  const handleImport = useCallback(() => {
    // Trigger file input click
    fileInputRef.current?.click();
  }, []);

  const handleFileChange = useCallback((event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.name.endsWith('.json')) {
      alert('Please select a JSON file');
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const jsonData = JSON.parse(e.target.result);
        importFromJSON(jsonData);
      } catch (error) {
        console.error('File read error:', error);
        alert(`Failed to read file: ${error.message}`);
      }
    };
    reader.onerror = () => {
      alert('Failed to read file');
    };
    reader.readAsText(file);

    // Reset file input so the same file can be selected again
    event.target.value = '';
  }, [importFromJSON]);

  return (
    <div
      className="w-screen h-screen grid"
      style={{
        gridTemplateColumns: "200px 1fr 250px",
        gap: 2,
        padding: 2,
        background: "#f7f7f7",
      }}
    >

      {/* LEFT: Palette */}
      <Card>

        <CardHeader>
          <CardTitle><center>Actions</center></CardTitle>
        </CardHeader>

        <CardContent>
          <div className="flex flex-col gap-2">
            {/* Default Actions */}
            <PaletteItem
              kind="httpAction"
              label="HTTP"
              icon={<LinkIcon size={20} />}
            />
            <PaletteItem
              kind="dbAction"
              label="Query"
              icon={<DatabaseIcon size={20} />}
            />
            <PaletteItem
              kind="terraformAction"
              label="Terraform"
              icon={<Cloud size={20} />}
            />
            <PaletteItem
              kind="jexl"
              label="JEXL"
              icon={<BracesIcon size={20} />}
            />
            <PaletteItem
              kind="branchAction"
              label="Branching"
              icon={<GitBranch size={20} />}
            />
            <PaletteItem
              kind="regexValidator"
              label="Regex"
              icon={<Shield size={20} />}
            />
            <PaletteItem
              kind="logger"
              label="Logger"
              icon={<FileText size={20} />}
            />
            <PaletteItem
              kind="delay"
              label="Delay"
              icon={<Clock size={20} />}
            />
            <PaletteItem
              kind="errorHandler"
              label="Error Handler"
              icon={<AlertCircle size={20} />}
            />
            <PaletteItem
              kind="terminateAction"
              label="Terminate"
              icon={<Power size={20} />}
            />
          {<br />}


            {/* Separator with Search */}
            {userDefinedActions.length > 0 && (
              <>
                <div className="border-t my-3"></div>
                <div className="flex items-center justify-between gap- mb-2">

                  {/* Search Box with Autocomplete */}
                  <div
                    className="relative flex-1 max-w-[200px]"
                    ref={searchInputRef}
                  >
                    <div className="relative">
                      <Input
                        type="text"
                        value={userActionSearch}
                        onChange={(e) => {
                          setUserActionSearch(e.target.value);
                          setShowAutocomplete(false);
                        }}
                        onFocus={() => setShowAutocomplete(false)}
                        onBlur={() => {
                          // Delay to allow click on autocomplete items
                          setTimeout(() => setShowAutocomplete(false), 200);
                        }}
                        placeholder="Workflow search..."
                        className="w-full pl-8 pr-7 py-1.5 text-xs border border-gray-200 rounded-md outline-none 
                                 transition-all duration-200 focus:border-blue-400 focus:ring-1 focus:ring-blue-200 
                                 bg-gray-50 hover:bg-white focus:bg-white"
                      />
                    </div>

                    {/* Autocomplete Dropdown */}
                    {showAutocomplete &&
                      autocompleteSuggestions.length > 0 &&
                      userActionSearch.trim() && (
                        <div
                          className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-xl 
                                      max-h-48 overflow-y-auto animate-in fade-in slide-in-from-top-1 duration-200"
                        >
                          {autocompleteSuggestions.map((action) => (
                            <button
                              key={action.id}
                              onClick={() => {
                                handleAutocompleteSelect(action);
                                setShowAutocomplete(false);
                              }}
                              onMouseDown={(e) => e.preventDefault()}
                              className="w-full px-3 py-2 text-left hover:bg-blue-50 active:bg-blue-100 
                                       flex items-center gap-2 text-xs transition-colors first:rounded-t-md last:rounded-b-md"
                            >
                              <span className="text-gray-600">
                                {action.icon}
                              </span>
                              <span className="text-gray-700">
                                {action.name}
                              </span>
                            </button>
                          ))}
                        </div>
                      )}
                  </div>
                </div>

                {/* Filtered User Defined Actions */}
                {filteredUserActions.length > 0 ? (
                  filteredUserActions.map((action) => (
                    <PaletteItem
                      key={action.id}
                      kind={action.type}
                      label={action.name}
                      icon={action.icon}
                    />
                  ))
                ) : userActionSearch.trim() ? (
                  <div className="text-xs text-gray-400 py-2 text-center">
                    No actions found
                  </div>
                ) : null}
              </>
            )}

          </div>
        </CardContent>
      </Card>

      {/* CENTER: Canvas */}
      <div
        className="h-full rounded-2xl border bg-white overflow-hidden"
        ref={flowRef}
      >
        <ReactFlow
          nodes={nodes}
          edges={styledEdges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          isValidConnection={isValidConnection}
          nodeTypes={allNodeTypes}
          onDrop={onDrop}
          onDragOver={onDragOver}
          fitView
          onNodeClick={handleNodeClick}
          onPaneClick={() => setSelectedId(null)}
        >
          <Background />
          <MiniMap pannable zoomable />
          <Controls />
          <Panel position="top-right">
            <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <Button
                onClick={handleRun}
                style={{ display: "flex", alignItems: "center", gap: 6 }}
              >
                <PlayIcon size={16} /> Run
              </Button>
              <Button 
                onClick={handleImport} 
                style={{ display: "flex", alignItems: "center", gap: 6 }}
              >
                <Upload size={16} /> Import
              </Button>
              <Button 
                onClick={handleExport} 
                style={{ display: "flex", alignItems: "center", gap: 6 }}
              >
                <FileText size={16} /> Export
              </Button>
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept=".json,application/json"
              onChange={handleFileChange}
              style={{ display: "none" }}
            />
          </Panel>
        </ReactFlow>
      </div>

      {/* RIGHT: Properties */}
      <PropertiesPanel
        selectedNode={selectedNode}
        onChange={handleChange}
        onDelete={handleDelete}
      />
      
      {/* Unified Panel Container with Terminal and Logs tabs */}
      <PanelContainer
        isVisible={showPanel}
        activeTab={activePanelTab}
        onTabChange={(tab) => {
          setActivePanelTab(tab);
          if (tab === 'terminal' && terminalInputRef.current) {
            setTimeout(() => terminalInputRef.current?.focus(), 100);
          }
        }}
        onClose={() => setShowPanel(false)}
        terminalProps={{
          output: terminalOutput,
          input: terminalInput,
          onInputChange: setTerminalInput,
          onExecute: () => executeTerminalCommand(terminalInput),
          onClear: clearTerminal,
          inputRef: terminalInputRef,
          containerRef: terminalContainerRef
        }}
        logsProps={{
          logs,
          onClear: clearLogs
        }}
      />
    </div>
  );
}
