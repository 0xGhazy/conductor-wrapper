import { Handle, Position } from "@xyflow/react";

function Badge({ color, icon, text }) {
  return (
    <div
      className="flex items-center gap-2 px-3 py-2 rounded-xl border bg-white shadow-sm"
      style={{ borderColor: color, borderRadius: 10 }}
    >
      <div
        className="p-1.5 rounded-lg"
        style={{ background: color + "22", color }}
      >
        {icon}
      </div>
      <span className="font-semibold text-sm" style={{ color }}>
        {text}
      </span>
    </div>
  );
}

export function FlagNode({
  color,
  icon,
  text,
  handleType,
  handlePosition,
  handleBeforeBadge = false,
}) {
  return (
    <div className="relative">
      {handleBeforeBadge && (
        <Handle type={handleType} position={handlePosition} />
      )}
      <Badge color={color} icon={icon} text={text} />
      {!handleBeforeBadge && (
        <Handle type={handleType} position={handlePosition} />
      )}
    </div>
  );
}
