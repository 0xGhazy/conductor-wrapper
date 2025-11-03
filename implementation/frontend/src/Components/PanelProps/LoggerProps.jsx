import React from "react";
import {Label, Input} from "../Inputs";

export default function LoggerProps({ data, onChange }) {
  const cfg = data.config || {};
  const update = (patch) => onChange({ config: { ...cfg, ...patch } });
  return (
    <>
      <div>
        <Label>Log Level</Label>
        <select
          value={cfg.level || "INFO"}
          onChange={(e) => update({ level: e.target.value })}
          className="w-full px-3 py-2 border rounded-lg"
        >
          {["DEBUG", "INFO", "WARN", "ERROR"].map((level) => (
            <option key={level} value={level}>{level}</option>
          ))}
        </select>
      </div>
      <div>
        <Label>Message</Label>
        <Input
          value={cfg.message || ""}
          placeholder="Log message"
          onChange={(e) => update({ message: e.target.value })}
        />
      </div>
    </>
  );
}
