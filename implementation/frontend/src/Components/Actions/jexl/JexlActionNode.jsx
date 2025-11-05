import {Label, Input} from "../../Inputs";
import { Braces as BracesIcon, Play as PlayIcon } from "lucide-react";
import { Handle, Position } from "@xyflow/react";

export function JexlActionProbs({ data, onChange }) {
  const cfg = data.config || {};
  return (
    <div>
      <Label>Expression</Label>
      <Input
        value={cfg.expression || ""}
        onChange={(e) => onChange({ config: { ...cfg, expression: e.target.value } })}
      />
    </div>
  );
}

export function JexlActionNode({ data, onRun }) {
  const handleRun = (e) => {
    e.stopPropagation();
    console.groupCollapsed(`Run JEXL node: ${data?.name || "JEXL"}`);
    console.log("node", {
      id: data?.id,
      kind: data?.kind,
      name: data?.name || "JEXL",
      config: data?.config,
      props: data,
    });
    console.groupEnd();
    if (typeof onRun === "function") onRun(data);
  };

  return (
    <div className="relative" style={{ pointerEvents: "all" }}>
      <Handle
        type="target"
        position={Position.Top}
        style={{ opacity: 6, pointerEvents: "auto" }}
      />

      <div className="relative rounded-2xl border bg-white min-w-[140px] overflow-hidden">
        <div
          className="flex items-center justify-between overflow-hidden relative"
          style={{ height: "42px" }}
        >
          {/* Left side: dynamic icon + label */}
          <div className="flex items-center gap-2 px-3">
            <div
              className="p-1 rounded-lg flex items-center justify-center"
              style={{ background: "#8b5cf620", width: "15px", height: "20px" }}
            >
                <BracesIcon size={16} />
            </div>
            <span className="font-medium text-sm">
              {data?.name || "JEXL"}
            </span>
          </div>

          {/* Run button */}
          <button
            type="button"
            onClick={handleRun}
            className="flex items-center justify-center h-full px-4 hover:bg-green-50 transition-colors absolute right-0 top-0"
            style={{
              background: "white",
              border: "none",
              borderTopRightRadius: "1rem",
              borderBottomRightRadius: "1rem",
            }}
            aria-label="run"
            title="execute"
          >
            <PlayIcon className="w-4 h-4" style={{ color: "#00de25ff" }} />
          </button>
        </div>
      </div>

      <div className="relative pb-4">
        <Handle
          type="source"
          position={Position.Bottom}
          id="output"
          style={{ bottom: "0px" }}
        />
      </div>
    </div>
  );
}
