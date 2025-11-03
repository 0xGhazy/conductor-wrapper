import React from "react";
import {Label, Input} from "../Inputs";

export default function TransformActionProps({ data, onChange }) {
  const cfg = data.config || {};
  return (
    <div>
      <Label>Expression</Label>
      <Input
        value={cfg.expression || ""}
        placeholder="({ out: input.value + 1 })"
        onChange={(e) => onChange({ config: { ...cfg, expression: e.target.value } })}
      />
    </div>
  );
}
