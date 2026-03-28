import { ProcessPlanScreen } from '@/components/process-plan-screen';

export default async function ProjectProcessPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;

  return <ProcessPlanScreen projectId={projectId} />;
}
