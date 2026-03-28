import { VisualQuestionScreen } from '@/components/visual-question-screen';

export default async function ProjectQaPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;

  return <VisualQuestionScreen projectId={projectId} />;
}
