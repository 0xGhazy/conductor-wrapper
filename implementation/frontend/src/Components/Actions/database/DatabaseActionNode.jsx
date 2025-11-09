import React from "react";
import { Label } from "../../Inputs";
// import { JsonInput } from "../../JsonInput";
import { Database as DatabaseIcon, Play as PlayIcon } from "lucide-react";
import { Handle, Position } from "@xyflow/react";


const DB_URL = "http://localhost:9090/api/actions/database";
const NONE = "--NONE--";

export function DatabaseActionProps({ data, onChange }) {
  const cfg = data?.config ?? {};
  const update = (patch) => onChange({ ...data, name: patch.name || data?.name, config: { ...cfg, ...patch } });

  const [queries, setQueries] = React.useState([NONE]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState("");
  const schemaCacheRef = React.useRef(new Map());
  const inFlightRef = React.useRef(null);
  const prevQueryRef = React.useRef(cfg.query ?? "");

  const fetchQueries = React.useCallback(async () => {
    setLoading(true); setError("");
    try {
      const r = await fetch(`${DB_URL}/queries`);
      if (!r.ok) throw new Error(`HTTP ${r.status}`);
      const j = await r.json();
      const names = Array.isArray(j) ? j.map(x => typeof x === "string" ? x : x?.name).filter(Boolean) : [];
      setQueries([NONE, ...Array.from(new Set(names))]);
    } catch (e) {
      if (e.name !== "AbortError") setError(String(e.message || e));
    } finally { setLoading(false); }
  }, []);

  React.useEffect(() => {
    const ctrl = new AbortController();
    fetchQueries();
    return () => ctrl.abort();
  }, [fetchQueries]);


  const loadParamSchema = async (q) => {
    if (!q || q === NONE) return null;
    if (schemaCacheRef.current.has(q)) return schemaCacheRef.current.get(q);


    if (inFlightRef.current) inFlightRef.current.abort();
    const ctrl = new AbortController();
    inFlightRef.current = ctrl;

    try {
      const r = await fetch(url);
      if (!r.ok) return null;
      const j = await r.json();
      schemaCacheRef.current.set(q, j);
      return j;
    } catch {
      return null;
    } finally {
      inFlightRef.current = null;
    }
  };

  const onQueryChange = async (e) => {
    const q = e.target.value;
    if (q === prevQueryRef.current) {
      update({ query: q, name: q === NONE ? "Query" : q });
      return;
    }
    prevQueryRef.current = q;
    update({ query: q, name: q === NONE ? "Query" : q });
  };

  const [paramSchema, setParamSchema] = React.useState(null);
  const handleOpenParams = async () => {
    const q = (cfg.query ?? "").trim();
    const s = await loadParamSchema(q);
    setParamSchema(s);
  };

  return (
    <>
      <div>
        <Label>Query</Label>
        <select
          value={cfg.query ?? NONE}
          onChange={onQueryChange}
          onFocus={() => fetchQueries()}
          className="w-full px-3 py-2 border rounded-lg"
        >
          {queries.map((q) => <option key={q} value={q}>{q}</option>)}
        </select>
        {loading && <div className="text-xs text-gray-500 mt-1">Loading…</div>}
        {error && <div className="text-xs text-red-600 mt-1">Failed: {error}</div>}
      </div>

      {paramSchema?.params?.length > 0 && (
        <div className="mt-3 space-y-2">
          <Label>Params</Label>
          {paramSchema.params.map((p) => (
            <div key={p.name} className="flex items-center gap-2">
              <label className="w-40 text-sm">{p.name}{p.required ? " *" : ""}</label>
              <input
                className="flex-1 px-3 py-2 border rounded-lg"
                placeholder={String(p.default ?? "")}
                value={(cfg.params?.[p.name] ?? "")}
                onChange={(e) => onParamChange(p.name, e.target.value)}
              />
            </div>
          ))}
        </div>
      )}
    </>
  );
}


export function DatabaseActionNode({ data, onRun }) {
  const  EXECUTION = "http://localhost:9090/api/actions/database/execute";

  const toJson = (v, fallback) => {
    if (v == null || v === "") return fallback;
    if (typeof v === "object") return v;
    try { return JSON.parse(v); } catch { return fallback; }
  };
  
  const handleRun = async (e) => {
    e.stopPropagation();
  
    const cfg = data?.config ?? {};
    const payload = {
      name: "DATABASE_WORKFLOW",
      type: "DATABASE_ACTION",
      config: {
        queryId: String(cfg.query).toUpperCase(),
        Params: toJson({}),
      }
    };
  
    console.groupCollapsed(`Run Database node: ${data?.name || "Database"}`);
    console.log("payload", payload);
    console.groupEnd();
  
    try {
      const res = await fetch(EXECUTION, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
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

  const engine = data?.config?.engine?.toLowerCase();

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
                <DatabaseIcon color="blue" size={16} />
            </div>
            <span className="font-medium text-sm">
              {data?.name || "Query"}
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
          id="databaseResult"
          style={{ bottom: "0px" }}
        />
      </div>
    </div>
  );
}
