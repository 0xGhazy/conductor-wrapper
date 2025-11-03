import React from "react";
import {Label, Input} from "../Inputs";

export default function RegexValidatorProps({ data, onChange }) {
  const cfg = data.config || {};
  return (
    <>
      <div>
        <Label>Pattern</Label>
        <Input
          value={cfg.pattern || ""}
          placeholder="/^[a-zA-Z0-9]+$/"
          onChange={(e) => onChange({ config: { ...cfg, pattern: e.target.value } })}
        />
      </div>
      <div>
        <Label>Field to Validate</Label>
        <Input
          value={cfg.field || ""}
          placeholder="input.value"
          onChange={(e) => onChange({ config: { ...cfg, field: e.target.value } })}
        />
      </div>
    </>
  );
}
