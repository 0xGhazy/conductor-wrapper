import React from "react";
import { Button, Input, Label, Select } from "../../Components/Inputs";
import "./Style.css";

export default function ConnectionModal({ open, onClose, connection, onSaved }) {
  if (!open) return null;

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
  const isApiKey = form.strategy === "API_KEY";
  const isOauth2 = form.strategy === "OAUTH2";

  const handleSubmit = async () => {
    setLoading(true);
    setError(""); setSuccess("");
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
      onSaved();
    } catch (e) {
      setError(e.message || "Failed to save connection");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="connection-form-modal">
      <div className="connection-form-card">
        <h2>{connection ? "Edit Connection" : "New Connection"}</h2>

        {/* NAME */}
        <div>
          <Label>Name *</Label>
          <Input value={form.name} onChange={(e) => update("name", e.target.value)} />
        </div>

        {/* STRATEGY */}
        <div>
          <Label>Strategy *</Label>
          <Select value={form.strategy} onChange={(v) => update("strategy", v)}>
            <option value="OAUTH2">OAUTH2</option>
            <option value="API_KEY">API_KEY</option>
          </Select>
        </div>

        {/* API KEY BLOCK */}
        {isApiKey && (
          <div className="p-4 border rounded-lg bg-gray-50 space-y-2">
            <h3 className="font-medium">API Key Settings</h3>
            <Label>API Key</Label>
            <Input value={form.apiKey} onChange={(e) => update("apiKey", e.target.value)} />
            <Label>API Key Header</Label>
            <Input value={form.apiKeyHeader} onChange={(e) => update("apiKeyHeader", e.target.value)} />
          </div>
        )}

        {/* OAUTH2 BLOCK */}
        {isOauth2 && (
          <div className="p-4 border rounded-lg bg-gray-50 space-y-2">
            <h3 className="font-medium">OAuth2 Settings</h3>

            <Label>Grant Type</Label>
            <Select value={form.grantType} onChange={(v) => update("grantType", v)}>
              <option value="">Select...</option>
              <option value="PASSWORD">PASSWORD</option>
              <option value="CLIENT_CREDENTIALS">CLIENT_CREDENTIALS</option>
              <option value="AUTHORIZATION_CODE">AUTHORIZATION_CODE</option>
            </Select>

            <Label>Client ID</Label>
            <Input value={form.clientId} onChange={(e) => update("clientId", e.target.value)} />

            <Label>Client Secret</Label>
            <Input value={form.clientSecret} onChange={(e) => update("clientSecret", e.target.value)} />

            {/* Conditional fields based on Grant Type */}
            {form.grantType === "PASSWORD" && (
              <>
                <Label>Username</Label>
                <Input value={form.username} onChange={(e) => update("username", e.target.value)} />
                <Label>Password</Label>
                <Input type="password" value={form.password} onChange={(e) => update("password", e.target.value)} />
              </>
            )}

            {form.grantType === "AUTHORIZATION_CODE" && (
              <>
                <Label>Code</Label>
                <Input value={form.code} onChange={(e) => update("code", e.target.value)} />
                <Label>Code Verifier</Label>
                <Input value={form.codeVerifier} onChange={(e) => update("codeVerifier", e.target.value)} />
                <Label>Redirect URI</Label>
                <Input value={form.redirectUri} onChange={(e) => update("redirectUri", e.target.value)} />
              </>
            )}

            <Label>Token Endpoint</Label>
            <Input value={form.tokenEndpoint} onChange={(e) => update("tokenEndpoint", e.target.value)} />

            <Label>Scope</Label>
            <Input value={form.scope} onChange={(e) => update("scope", e.target.value)} />
          </div>
        )}

        {error && <p className="text-error">{error}</p>}
        {success && <p className="text-success">{success}</p>}

        <div className="form-actions">
          <Button onClick={handleSubmit} disabled={loading}>{loading ? "Saving…" : "Save"}</Button>
          <Button onClick={onClose} className="btn-cancel">Cancel</Button>
        </div>
      </div>
    </div>
  );
}
