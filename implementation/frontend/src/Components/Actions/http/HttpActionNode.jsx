import React from "react";
import { Link as LinkIcon } from "lucide-react";
import { Position } from "@xyflow/react";
import JsonInput from "../../JsonInput";
import {Label, Input} from "../../Inputs";
import { ActionNode } from "../../flow/ActionNode";


export function HttpActionProps({ data, onChange }) {
  const cfg = data.config || {};
  const update = (patch) => onChange({ config: { ...cfg, ...patch } });

  // TODO: Implement list connection API call here

  return (
    <>
      <div>
        <Label>Method</Label>
        <select
          value={cfg.method || "GET"}
          onChange={(e) => update({ method: e.target.value })}
          className="w-full px-3 py-2 border rounded-lg"
        >
          {["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"].map((m) => (
            <option key={m} value={m}>{m}</option>
          ))}
        </select>
      </div>

      <div>
        <Label>URL</Label>
        <Input
          value={cfg.url || ""}
          placeholder="https://api.example.com"
          onChange={(e) => update({ url: e.target.value })}
        />
      </div>

      <JsonInput label="Headers (JSON)" value={cfg.headers} onChange={(v) => update({ headers: v })} />
      <JsonInput label="Query (JSON)"   value={cfg.query}   onChange={(v) => update({ query: v })} />
      <JsonInput label="Body (JSON)"    value={cfg.body}    onChange={(v) => update({ body: v })} allowEmpty />

      <div>
        <Label>Timeout (sec)</Label>
        <Input
          type="number"
          placeholder={30}
          value={cfg.timeout ?? 30}
          onChange={(e) => update({ timeout: Number(e.target.value) })}
        />
      </div>

      <div>
        <Label>Connection</Label>
        <select
          value={"loyalty-ck"}
          placeholder="connection string"
          onChange={(e) => update({ connection: e.target.value })}
          className="w-full px-3 py-2 border rounded-lg"
        >
          {["loyalty-ck", "shipment_and_track_kc"].map((m) => (
            <option key={m} value={m}>{m}</option>
          ))}
        </select>
      </div>
    </>
  );
}



export function HttpActionNode({ data }) {
    const method = String((data && data.config && data.config.method) || "GET").toUpperCase();
    const theme = METHOD_THEME[method] || METHOD_THEME.DEFAULT;

    // TODO: Implement RUN method for this action
    const runHttp = (node) => {
      const method = String(node?.config?.method || "GET").toUpperCase();
      console.log("execute HTTP node only", {
        method,
        url: node?.config?.url,
        headers: node?.config?.headers,
        body: node?.config?.body,
        connection: node?.config.connection,
        timeout: node?.config.timeout,
      });
    };
  
    return (
      <ActionNode
        data={data}
        icon={<LinkIcon size={16} />}
        accent={theme.accent}
        defaultTitle="HTTP"
        targetHandlePosition={Position.Top}
        sourceHandlePosition={Position.Bottom}
        containerStyle={{ "--accent": theme.accent }}
        containerClassName="transition-shadow hover:ring-2 ring-[--accent]"
        onRun={runHttp}
      />
    );
}


/* Helpers */
const METHOD_THEME = {
    GET:      { accent: "#00FF17", badge: "bg-green-50 border-sky-200 text-sky-700" },
    POST:     { accent: "#ef4444", badge: "bg-rose-50 border-rose-200 text-rose-700" },
    PUT:      { accent: "#f59e0b", badge: "bg-amber-50 border-amber-200 text-amber-700" },
    PATCH:    { accent: "#8b5cf6", badge: "bg-violet-50 border-violet-200 text-violet-700" },
    DELETE:   { accent: "#BD421E", badge: "bg-red-50 border-red-200 text-red-700" },
    OPTIONS:  { accent: "#f43f5e", badge: "bg-pink-50 border-red-200 text-red-700" },
    HEAD:     { accent: "#00FF17", badge: "bg-green-50 border-green-200 text-green-700" },
    DEFAULT:  { accent: "#64748b", badge: "bg-slate-50 border-slate-200 text-slate-700" },
};
