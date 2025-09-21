import { z } from "zod";

export const StatusSchema = z.object({
  id: z.string(),
  label: z.string().min(1),
  position: z.object({ x: z.number(), y: z.number() }),
  initial: z.boolean().optional(),
  terminal: z.boolean().optional(),
});

export const TransitionSchema = z.object({
  id: z.string(),
  from: z.string(),
  to: z.string(),
  label: z.string().optional(),
  guard: z.string().optional(),
  phases: z
    .object({
      before: z.string().optional(),
      during: z.string().optional(),
      after: z.string().optional(),
    })
    .optional(),
});

export const BlueprintSchema = z.object({
  statuses: z.array(StatusSchema),
  transitions: z.array(TransitionSchema),
});

export type Status = z.infer<typeof StatusSchema>;
export type Transition = z.infer<typeof TransitionSchema>;
export type Blueprint = z.infer<typeof BlueprintSchema>;
