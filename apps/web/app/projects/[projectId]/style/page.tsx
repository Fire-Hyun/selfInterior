import { StyleGenerationScreen } from '@/components/style-generation-screen';

export default async function ProjectStylePage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;

  return <StyleGenerationScreen projectId={projectId} />;
}
