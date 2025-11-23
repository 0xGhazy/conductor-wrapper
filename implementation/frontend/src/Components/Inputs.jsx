export function Label({ children }) {
    return (
      <label className="block text-xs font-semibold text-gray-700 mb-1">
        {children}
      </label>
    );
}

export function Input(props) {
    return (
      <input
        {...props}
        className={`w-full px-3 py-2 border rounded-lg outline-none ${
          props.className || ""
        }`}
      />
    );
}

export function Select({ label, value, onChange, children, className = "" }) {
  return (
    <div className="flex flex-col gap-1 w-full">
      {label && <label className="text-sm font-medium">{label}</label>}

      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className={
          "border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 " +
          className
        }
      >
        {children}
      </select>
    </div>
  );
}


export function Button({ children, onClick, variant = "default", ...props }) {
    const styles =
      variant === "danger"
        ? "bg-red-600 hover:bg-red-700 text-white"
        : variant === "secondary"
        ? "bg-gray-100 hover:bg-gray-200 text-gray-900"
        : "bg-blue-600 hover:bg-blue-700 text-white";
    return (
      <button
        onClick={onClick}
        className={`px-3 py-2 rounded-lg text-sm font-medium ${styles}`}
        {...props}
      >
        {children}
      </button>
    );
}

export function Card({ children }) {
    return (
      <div className="bg-white border rounded-1xl shadow-sm">{children}</div>
    );
}

export function CardHeader({ children }) {
    return <div className="px-4 py-9 border-b">{children}</div>;
}

export function CardTitle({ children }) {
    return <div className="font-semibold">{children}</div>;
}

export function CardContent({ children }) {
    return <div className="p-4">{children}</div>;
}
