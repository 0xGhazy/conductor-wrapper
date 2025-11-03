import React from "react";
import {Label, Input} from "../Inputs";

export default function BranchActionProps({ data, onChange }) {
  const cfg = data.config || {};
  return (
    <div>
      <Label>Condition</Label>
      <Input
        value={cfg.condition || ""}
        placeholder="input.value > 0"
        onChange={(e) => onChange({ config: { ...cfg, condition: e.target.value } })}
      />
      <div className="text-xs text-gray-500 mt-1">
        True connects to the left output, False to the right.
      </div>
    </div>
  );
}
