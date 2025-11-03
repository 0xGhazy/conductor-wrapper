import React from "react";

import JsonInput from "../JsonInput";
import {Label, Input} from "../Inputs";

export default function DbActionProps({ data, onChange }) {
  const cfg = data.config || {};
  const update = (patch) => onChange({ config: { ...cfg, ...patch } });

  return (
    <>
      <div>
        <Label>Engine</Label>
        <select
          value={cfg.engine || "postgresql"}
          onChange={(e) => update({ engine: e.target.value })}
          className="w-full px-3 py-2 border rounded-lg"
        >
          <option value="postgresql">PostgreSQL</option>
          <option value="mysql">MySQL</option>
          <option value="sqlite">SQLite</option>
        </select>
      </div>

      <div>
        <Label>Connection</Label>
        <Input
          value={cfg.connection || ""}
          placeholder="postgresql://user:pass@host:5432/db"
          onChange={(e) => update({ connection: e.target.value })}
        />
      </div>

      <div>
        <Label>SQL</Label>
        <Input
          value={cfg.sql || ""}
          placeholder="INSERT INTO table(col) VALUES(:val)"
          onChange={(e) => update({ sql: e.target.value })}
        />
      </div>

      <JsonInput label="Params (JSON)" value={cfg.params} onChange={(v) => update({ params: v })} />
    </>
  );
}
