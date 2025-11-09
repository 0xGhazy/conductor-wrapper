import { Label } from "../../Inputs";
import { Cloud as TerraformIcon, Play as PlayIcon } from "lucide-react";
import { Handle, Position } from "@xyflow/react";


export function TerraformActionProps({ data, onChange }) {
  const cfg = data.config || {};

  const update = (patch) =>
    onChange({
      ...data,
      action: patch.action || data.action,
      config: { ...cfg, ...patch },
    });


 return (
    <>
      <div>
        <Label>Action</Label>
        <select
          value={cfg.action || "Create new ES2 Instance"}
          onChange={(e) =>
            update({
              query: e.target.value,
              name: e.target.value, // sync name with selected option
            })
          }
          className="w-full px-3 py-2 border rounded-lg"
        >
            <option value="createNewEs2Instance">Create new ES2 Instance</option>
            <option value="Add2gMemory">Add 2GB Memory</option>
        </select>

        {/* <Label>Params</Label> */}
        {/* <JsonInput label="Params (JSON)"    value={cfg.params}    onChange={(v) => update({ params: v })} allowEmpty /> */}
      </div>
    </>
  );
}


export function TerraformActionNode({ data, onRun }) {
  const handleRun = (e) => {
    e.stopPropagation();
    console.groupCollapsed(`Run Terraform node: ${data?.name || "Terraform"}`);
    console.log("node", {
      id: data?.id,
      kind: data?.kind,
      name: data?.name || "Terraform",
      config: data?.config,
      props: data,
    });
    console.groupEnd();
    if (typeof onRun === "function") onRun(data);
  };

  const engine = data?.config?.engine?.toLowerCase();

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
                <TerraformIcon size={16} />
            </div>
            <span className="font-medium text-sm">
              {data?.name || "Terraform"}
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
