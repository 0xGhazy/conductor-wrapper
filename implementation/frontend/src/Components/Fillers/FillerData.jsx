import { Workflow as WorkflowIcon } from "lucide-react";

export const initialNodes = [
    {
      id: "start",
      type: "startFlag",
      position: { x: 60, y: 140 },
      data: {name: "START"},
      draggable: false,
      selectable: false,
    },
];

// User-defined actions configuration
export const userDefinedActions = [
    {
      id: "getCustomerBalanceByMsisdn",
      name: "Get customer balance by MSISDN",
      type: "customAction1",
      icon: <WorkflowIcon size={20} />,
      accent: "#6366f1",
      defaultConfig: { message: "Custom action 1" },
    },
    {
      id: "burnPointsByAccountId",
      name: "Burn points by account id",
      type: "customAction2",
      icon: <WorkflowIcon size={20} />,
      accent: "#ec4899",
      defaultConfig: { value: "Custom action 2" },
    },
    {
      id: "opt_out_from_program_by_program_id",
      name: "opt-out from program by program id",
      type: "customAction3",
      icon: <WorkflowIcon size={20} />,
      accent: "#ec4899",
      defaultConfig: { value: "Custom action 3" },
    }
  ];