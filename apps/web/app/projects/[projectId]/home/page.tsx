import { ProjectHomeScreen } from '@/components/project-home-screen';

export default async function ProjectHomePage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;

  return <ProjectHomeScreen projectId={projectId} />;
}
