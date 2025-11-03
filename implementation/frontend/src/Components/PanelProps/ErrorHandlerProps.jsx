import React from "react";
import JsonInput from "../JsonInput";
import {Label, Input} from "../Inputs";


export default function ErrorHandlerProps({ data, onChange }) {
  const cfg = data.config || {};
  const update = (patch) => onChange({ config: { ...cfg, ...patch } });

  return (
    <>
      <div>
        <Label>Error Type</Label>
        <Input
          value={cfg.errorType || ""}
          placeholder="Any error"
          onChange={(e) => update({ errorType: e.target.value })}
        />
      </div>

      <JsonInput
        label="Fallback Value (JSON)"
        value={cfg.fallbackValue ?? null}
        placeholder='{"error":"handled"}'
        onChange={(v) => update({ fallbackValue: v })}
        allowEmpty
      />
    </>
  );
}
