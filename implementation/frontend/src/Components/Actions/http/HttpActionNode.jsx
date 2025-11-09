import React, { useEffect, useMemo, useState } from "react";
import { Link as LinkIcon, Play as PlayIcon } from "lucide-react";
import JsonInput from "../../JsonInput";
import {Label, Input} from "../../Inputs";
import { Handle, Position } from "@xyflow/react";

const CONN_URL = "http://localhost:9090/api/actions/http";
const NONE = "--NONE--";

export function HttpActionProps({ data, onChange }) {
  const cfg = data?.config ?? {};
  const update = (patch) => onChange({ config: { ...cfg, ...patch } });

  const [items, setItems] = React.useState([NONE]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState("");

  const fetchConnections = React.useCallback(async (signal) => {
    setLoading(true); setError("");
    try {
      const res = await fetch(CONN_URL + "/connections", { signal });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const json = await res.json();
      const names = Array.isArray(json)
        ? json.map((x) => (typeof x === "string" ? x : x?.name)).filter(Boolean)
        : [];
      setItems([NONE, ...Array.from(new Set(names))]);
    } catch (e) {
      if (e.name !== "AbortError") setError(String(e.message || e));
    } finally { setLoading(false); }
  }, []);

  React.useEffect(() => {
    const ctrl = new AbortController();
    fetchConnections(ctrl.signal);
    const onFocus = () => fetchConnections(new AbortController().signal);
    window.addEventListener("focus", onFocus);
    const id = setInterval(() => {
      fetchConnections(new AbortController().signal);
    }, 60_000);

    return () => { clearInterval(id); window.removeEventListener("focus", onFocus); ctrl.abort(); };
  }, [fetchConnections]);

  const onConnChange = (e) => {
    const v = e.target.value;
    update({ connection: v === NONE ? null : v });
  };

  // optional: refresh when user opens the select
  const onConnFocus = () => fetchConnections(new AbortController().signal);

  const options = React.useMemo(() => {
    const set = new Set(items);
    if (cfg.connection && !set.has(cfg.connection)) {
      return [NONE, cfg.connection, ...items.filter((x) => x !== NONE)];
    }
    return items;
  }, [items, cfg.connection]);

  return (
    <>
      <div>
        <Label>Method</Label>
        <select
          value={cfg.method || "GET"}
          onChange={(e) => update({ method: e.target.value })}
          className="w-full px-3 py-2 border rounded-lg"
        >
          {["GET","POST","PUT","PATCH","DELETE","OPTIONS","HEAD"].map((m) => (
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
        <Label>Timeout (Ms)</Label>
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
            value={cfg.connection ?? NONE}
            onChange={onConnChange}
            className="w-full px-3 py-2 border rounded-lg"
          >
            {options.map((m) => (
              <option key={m} value={m}>{m}</option>
            ))}
          </select>
          {loading && <div className="text-xs text-gray-500 mt-1">Loading…</div>}
      </div>
    </>
  );
}


export function HttpActionNode({ data }) {
    const apiUrl = CONN_URL+ "/execute";

    const toJson = (v, fallback) => {
      if (v == null || v === "") return fallback;
      if (typeof v === "object") return v;
      try { return JSON.parse(v); } catch { return fallback; }
    };
    
    const handleRun = async (e) => {
      e.stopPropagation();
    
      const cfg = data?.config ?? {};
      const payload = {
        type: "HTTP_ACTION",
        config: {
          url: cfg.url ?? "",
          method: String(cfg.method ?? "GET").toUpperCase(),
          headers: toJson(cfg.headers, {}),
          query:   toJson(cfg.query,   {}),
          body:    toJson(cfg.body,    {}),
          connection: cfg.connection ?? "--NONE--",
          timeout: Number(cfg.timeout ?? 5),
        }
      };
    
      console.groupCollapsed(`Run HTTP node: ${data?.name || "HTTP"}`);
      console.log("payload", payload);
      console.groupEnd();
    
      try {
        const timeoutMs = (payload.timeout ?? 30) * 1000;
        const signal = typeof AbortSignal?.timeout === "function"
          ? AbortSignal.timeout(timeoutMs)
          : (() => {
              const ctrl = new AbortController();
              setTimeout(() => ctrl.abort(), timeoutMs);
              return ctrl.signal;
            })();
    
        const res = await fetch(apiUrl, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
          signal,
        });
    
        const contentType = res.headers.get("content-type") || "";
        const dataOut = contentType.includes("application/json")
          ? await res.json()
          : await res.text();
    
        if (!res.ok) {
          console.error("HTTP execute failed", { status: res.status, dataOut });
          alert(`HTTP execute failed: ${res.status}\n${typeof dataOut === "string" ? dataOut : JSON.stringify(dataOut, null, 2)}`);
          return;
        }
    
        console.log("HTTP execute success", dataOut);
        alert(`HTTP execute success:\n${typeof dataOut === "string" ? dataOut : JSON.stringify(dataOut, null, 2)}`);
      } catch (err) {
        console.error("HTTP execute error", err);
        alert(`HTTP execute error: ${err?.name || "Error"}\n${err?.message || String(err)}`);
      }
    
      if (typeof onRun === "function") onRun(data);
    };


    return (
      <div className="relative" style={{ pointerEvents: "all" }}>
        <Handle
          type="target"
          position={Position.Top}
          style={{ opacity: 0, pointerEvents: "auto" }}
        />
  
        <div className="relative rounded-2xl border bg-white min-w-[140px] overflow-hidden">
          <div
            className="flex items-center justify-between overflow-hidden relative"
            style={{ height: "42px" }}
          >
            {/* Left side: dynamic icon + label */}
            <div className="flex items-center gap-2 px-3">
              <div
                className="p-1 rounded-lg flex items-center justify-center"
                style={{ background: "#8b5cf620", width: "15px", height: "20px" }}
              >
                   <LinkIcon color="#df1111" />
              </div>
              <span className="font-medium text-sm">
                {data?.name || "HTTP"}
              </span>
            </div>
  
            {/* Run button */}
            <button
              type="button"
              onClick={handleRun}
              className="flex items-center justify-center h-full px-4 hover:bg-green-50 transition-colors absolute right-0 top-0"
              style={{
                background: "white",
                border: "none",
                borderTopRightRadius: "1rem",
                borderBottomRightRadius: "1rem",
              }}
              aria-label="run"
              title="Run"
            >
              <PlayIcon className="w-4 h-4" style={{ color: "#00de25ff" }} />
            </button>
          </div>
        </div>
  
        <div className="relative pb-4">
          <Handle
            type="source"
            position={Position.Bottom}
            id="httpResult"
            style={{ bottom: "0px" }}
          />
        </div>
      </div>
    );
}
