import { ExpertLeadScreen } from '@/components/expert-lead-screen';

export default async function ProjectExpertsPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;

  return <ExpertLeadScreen projectId={projectId} />;
}
