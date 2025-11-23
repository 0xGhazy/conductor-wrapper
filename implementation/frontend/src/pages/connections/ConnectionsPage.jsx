import React, { useEffect, useState } from "react";
import ConnectionModal from "./ConnectionModal";
import "./Style.css";
import vodafoneLogo from "../../assets/vodafone-logo.png";
import { FaTrash, FaEdit } from "react-icons/fa";

export default function ConnectionsPage() {
  const [connections, setConnections] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedConnection, setSelectedConnection] = useState(null);
  const API_URL = "http://localhost:9090/api/actions/http/connections";

const fetchConnections = async () => {
  setLoading(true);
  setError("");

  try {
    const res = await fetch(API_URL);
    if (!res.ok) {
      console.log(`API response: ${res}`);
      throw new Error("Backend API ERROR");
    }

    const json = await res.json();
    // Map API response to table-friendly format
    const mappedConnections = (json.data || []).map((c) => ({
      id: c.id,
      name: c.name,
      strategy: c.strategy,
      grantType: "-", // grantType not returned by API
      status: c.active ? "Active" : "Inactive",
      createdAt: new Date(c.createdAt).toLocaleString(),
      updatedAt: c.updatedAt ? new Date(c.updatedAt).toLocaleString() : "-",
    }));
    setConnections(mappedConnections);
  } catch (e) {
    setError(e.message || "Failed to fetch connections");
  } finally {
    setLoading(false);
  }
};


  useEffect(() => { fetchConnections(); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this connection?")) return;
    try {
      const res = await fetch(`/api/connections/${id}`, { method: "DELETE" });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setConnections(connections.filter((c) => c.id !== id));
    } catch (e) {
      alert(e.message || "Failed to delete connection");
    }
  };

  const openModal = (connection=null) => {
    setSelectedConnection(connection);
    setModalOpen(true);
  };

  return (
    <div className="connections-page">
  {/* Header */}
  <header className="connections-header">
    <div className="header-left">
      <img src={vodafoneLogo} alt="Vodafone" className="logo" />
      <h1>Connections</h1>
    </div>
  </header>

  {/* Centered content container */}
  <div className="connections-content">
    {/* New Connection Button */}
    <div className="new-connection-button">
      <button className="btn-create" onClick={() => openModal()}>+ New Connection</button>
    </div>

    {error && <p className="text-error">{error}</p>}

    {loading ? <p>Loading…</p> : (
      <div className="connections-table-container">
        <table className="connections-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Strategy</th>
              <th>Status</th>
              <th>Creation time</th>
              <th>Update time</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {connections.map((c) => (
              <tr key={c.id}>
                <td>{c.name}</td>
                <td>{c.strategy}</td>
                <td>{c.status}</td>
                <td>{c.createdAt}</td>
                <td>{c.updatedAt}</td>
                <td className="actions-cell">
                  <button className="btn-icon" onClick={() => openModal(c)} title="Edit">
                    <FaEdit size={18}/>
                  </button>
                  <button className="btn-icon" onClick={() => handleDelete(c.id)} title="Delete">
                    <FaTrash size={18}/>
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )}
  </div>

  <ConnectionModal
    open={modalOpen}
    connection={selectedConnection}
    onClose={() => { setModalOpen(false); fetchConnections(); }}
    onSaved={() => { setModalOpen(false); fetchConnections(); }}
  />
</div>
  );
}
