import { Handle, Position } from "@xyflow/react";
import { Play as PlayIcon /* ...others */ } from "lucide-react";

export function NodeShell({ title, icon, accent, onRun }) {
  return (
    <div className="rounded-2xl border bg-white min-w-[220px] relative">
      <div
        className="flex items-center gap-2 px-3 py-2 border-b relative"
        style={{ borderColor: accent }}
      >
        <div className="p-1 rounded-lg" style={{ background: accent + "20" }}>
          {icon}
        </div>
        <span className="font-medium text-sm">{title}</span>

        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onRun && onRun();
          }}
          className="relative right-2 top-1.5 p-1 rounded-md hover:bg-green-50 focus:outline-none"
          aria-label="run"
          title="run"
        >
          <PlayIcon className="w-4 h-4" style={{ color: "#16a34a" }} />
        </button>
      </div>
    </div>
  );
}

export function ActionNode({
  data,
  icon,
  accent = "#8b5cf6",
  defaultTitle = "Node",
  targetHandlePosition = Position.Top,
  targetHandleProps = {},
  sourceHandlePosition,
  sourceHandles,
  containerClassName = "",
  containerStyle = {},
  renderContent,
  onRun,
}) {
  const title = (data && data.name) || defaultTitle;

  const handleRun = () => {
    console.groupCollapsed(`Run node: ${title}`);
    console.log("node", {
      id: data && data.id,
      kind: data && data.kind,
      name: title,
      config: data && data.config,
      props: data,
    });
    console.groupEnd();
    if (typeof onRun === "function") onRun(data);
  };

  return (
    <div className="relative" style={{ pointerEvents: "all" }}>
      <Handle
        type="target"
        position={targetHandlePosition}
        style={{ pointerEvents: "auto", ...(targetHandleProps.style || {}) }}
        {...targetHandleProps}
      />

      <div
        className={`rounded-2xl border bg-white min-w-[220px] ${containerClassName}`}
        style={containerStyle}
      >
        <NodeShell title={title} icon={icon} accent={accent} onRun={handleRun} />
        {renderContent && renderContent(data)}
      </div>

      <div className="relative pb-4">
        {Array.isArray(sourceHandles) && sourceHandles.length > 0 ? (
          sourceHandles.map((h) => (
            <React.Fragment key={h.id}>
              <Handle
                type="source"
                id={h.id}
                position={h.position || Position.Bottom}
                style={h.style}
                {...(h.handleProps || {})}
              />
              {h.label ? (
                <span
                  className="absolute text-[10px] text-gray-500 font-medium"
                  style={{
                    bottom:
                      typeof (h.style && h.style.bottom) !== "undefined"
                        ? `calc(${h.style.bottom} - 16px)`
                        : "-20px",
                    left: h.style && h.style.left,
                    right: h.style && h.style.right,
                    transform:
                      h.style &&
                      typeof h.style.left === "string" &&
                      h.style.left.includes("%")
                        ? "translateX(-50%)"
                        : undefined,
                  }}
                >
                  {h.label}
                </span>
              ) : null}
            </React.Fragment>
          ))
        ) : (
          sourceHandlePosition ? (
            <Handle type="source" position={sourceHandlePosition} />
          ) : null
        )}
      </div>
    </div>
  );
}

export default ActionNode;
