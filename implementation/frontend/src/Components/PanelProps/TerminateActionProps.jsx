import React from "react";
import {Label, Input} from "../Inputs";

export default function TerminateActionProps({ data, onChange }) {
  const cfg = data.config || {};
  return (
    <div>
      <Label>Reason</Label>
      <Input
        value={cfg.reason || ""}
        placeholder="Workflow terminated"
        onChange={(e) => onChange({ config: { ...cfg, reason: e.target.value } })}
      />
    </div>
  );
}
