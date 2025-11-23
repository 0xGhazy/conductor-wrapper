import React from "react";
import { Button, Input, Label, Select } from "../../Components/Inputs";
import "./Style.css";


export default function ConnectionForm({ connection, isEditing, onClose }) {
  const INIT = connection || {
    id: null,
    name: "",
    strategy: "OAUTH2",
    apiKey: "",
    apiKeyHeader: "",
    grantType: "PASSWORD",
    clientId: "",
    clientSecret: "",
    username: "",
    password: "",
    tokenEndpoint: "",
    scope: "",
    code: "",
    codeVerifier: "",
    redirectUri: ""
  };

  const [form, setForm] = React.useState(INIT);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState("");
  const [success, setSuccess] = React.useState("");

  const update = (key, val) => setForm((f) => ({ ...f, [key]: val }));

  const handleSubmit = async () => {
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const method = connection ? "PUT" : "POST";
      const url = connection ? `/api/connections/${connection.id}` : "/api/connections";
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setSuccess(connection ? "Updated successfully" : "Created successfully");
      setTimeout(() => onClose(), 1000);
    } catch (e) {
      setError(e.message || "Failed to save connection");
    } finally {
      setLoading(false);
    }
  };

  const isApiKey = form.strategy === "API_KEY";
  const isOauth2 = form.strategy === "OAUTH2";

  return (
    <div className="connection-form-modal">
      <div className="connection-form-card">
        <h2>{connection ? (isEditing ? "Edit Connection" : "View Connection") : "New Connection"}</h2>
        
        {/* Form fields (reuse from CreateConnectionPage) */}
        <div>
          <Label>Name *</Label>
          <Input value={form.name} onChange={(e) => update("name", e.target.value)} disabled={!isEditing} />
        </div>

        <div>
          <Label>Strategy *</Label>
          <Select value={form.strategy} onChange={(v) => update("strategy", v)} disabled={!isEditing}>
            <option value="OAUTH2">OAUTH2</option>
            <option value="API_KEY">API_KEY</option>
          </Select>
        </div>

        {isApiKey && (
          <div className="p-4 border rounded-lg space-y-3 bg-gray-50">
            <h3>API Key Settings</h3>
            <Label>API Key</Label>
            <Input value={form.apiKey} onChange={(e) => update("apiKey", e.target.value)} disabled={!isEditing} />
            <Label>API Key Header</Label>
            <Input value={form.apiKeyHeader} onChange={(e) => update("apiKeyHeader", e.target.value)} disabled={!isEditing} />
          </div>
        )}

        {isOauth2 && (
          <div className="p-4 border rounded-lg space-y-3 bg-gray-50">
            <h3>OAuth2 Settings</h3>
            <Label>Grant Type</Label>
            <Select value={form.grantType} onChange={(v) => update("grantType", v)} disabled={!isEditing}>
              <option value="PASSWORD">PASSWORD</option>
              <option value="CLIENT_CREDENTIALS">CLIENT_CREDENTIALS</option>
              <option value="AUTHORIZATION_CODE">AUTHORIZATION_CODE</option>
            </Select>
            <Label>Client ID</Label>
            <Input value={form.clientId} onChange={(e) => update("clientId", e.target.value)} disabled={!isEditing} />
            <Label>Client Secret</Label>
            <Input value={form.clientSecret} onChange={(e) => update("clientSecret", e.target.value)} disabled={!isEditing} />
          </div>
        )}

        {error && <p className="text-error">{error}</p>}
        {success && <p className="text-success">{success}</p>}

        <div className="form-actions">
          {isEditing && <Button onClick={handleSubmit} disabled={loading}>{loading ? "Saving…" : "Save"}</Button>}
          <Button onClick={onClose} className="btn-cancel">Close</Button>
        </div>
      </div>
    </div>
  );
}
