import React from "react";
import {Label, Input} from "../Inputs";

export default function DelayProps({ data, onChange }) {
  const cfg = data.config || {};
  return (
    <div>
      <Label>Duration (ms)</Label>
      <Input
        type="number"
        value={cfg.duration ?? 0}
        placeholder="1000"
        onChange={(e) => onChange({ config: { ...cfg, duration: Number(e.target.value) } })}
      />
    </div>
  );
}
