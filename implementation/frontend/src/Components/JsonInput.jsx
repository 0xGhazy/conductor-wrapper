import React from "react";

function Label({ children }) {
  return (
    <label className="block text-xs font-semibold text-gray-700 mb-1">
      {children}
    </label>
  );
}


function Input(props) {
  return (
    <input
      {...props}
      className={`w-full px-3 py-2 border rounded-lg outline-none ${
        props.className || ""
      }`}
    />
  );
}

export default function JsonInput({ label, value, onChange, placeholder="{}", allowEmpty=false }) {
  const [text, setText] = React.useState(
    typeof value === "string" ? value : JSON.stringify(value ?? (allowEmpty ? "" : {}))
  );
  const [error, setError] = React.useState("");

  const handle = (e) => {
    const v = e.target.value;
    setText(v);
    try {
      const parsed = v === "" && allowEmpty ? "" : JSON.parse(v);
      setError("");
      onChange(parsed);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <Label>{label}</Label>
      <Input value={text} placeholder={placeholder} onChange={handle} />
      {error && <div className="text-xs text-red-600 mt-1">Invalid JSON: {error}</div>}
    </div>
  );
}
