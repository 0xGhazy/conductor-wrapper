import React from "react";
import { Regex as RegexIcon, Play as PlayIcon } from "lucide-react";
import {Label, Input} from "../../Inputs";
import { Handle, Position } from "@xyflow/react";


export function RegexActionProps({ data, onChange }) {
  const cfg = data.config || {};
  return (
    <>
      <div>
        <Label>Pattern</Label>
        <Input
          value={cfg.pattern || ""}
          placeholder=""
          onChange={(e) => onChange({ config: { ...cfg, pattern: e.target.value } })}
        />
      </div>
      <div>
        <Label>Field to Validate</Label>
        <Input
          value={cfg.field || ""}
          placeholder=""
          onChange={(e) => onChange({ config: { ...cfg, field: e.target.value } })}
        />
      </div>
    </>
  );
}


export function RegexActionNode({ data }) {

    const handleRun = () => {
        try {
          const cfg = data?.config ?? {};
          const pattern = cfg.pattern || "";
          const field = cfg.field || "";
          if (!pattern || !field) {
            alert("Pattern and input field are required");
            return;
          }
    
        //   const value =
        //     (data?.input && data.input[field] != null ? data.input[field] : data?.[field]) ?? "";
    
          const re = new RegExp(pattern);
          const isValid = re.test(String(field));
    
          alert(isValid ? "Valid ✅" : "Invalid ❌");
        } catch (err) {
          alert("Error" + err);
          console.error(err);
        }
      };

    return (
      <div className="relative" style={{ pointerEvents: "all" }}>
        <Handle
          type="target"
          position={Position.Top}
          style={{ opacity: 0, pointerEvents: "auto" }}
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
                   <RegexIcon color="#11b63a" />
              </div>
              <span className="font-medium text-sm">
                {data?.name || "Regex"}
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
              title="Run"
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
