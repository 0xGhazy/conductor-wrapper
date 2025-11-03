import React from "react";
import {Label, Input} from "../Inputs";

export default function GenericUserActionProps({ data, onChange }) {
  const cfg = data.config || {};
  const keys = Object.keys(cfg);

  if (!keys.length) return <div className="text-sm text-gray-500">No configurable fields.</div>;

  return (
    <div>
      <Label>Configuration</Label>
      {keys.map((key) => (
        <div key={key} className="mb-2">
          <Label>{key.charAt(0).toUpperCase() + key.slice(1)}</Label>
          <Input
            value={cfg[key] ?? ""}
            placeholder={`Enter ${key}`}
            onChange={(e) => onChange({ config: { ...cfg, [key]: e.target.value } })}
          />
        </div>
      ))}
    </div>
  );
}
