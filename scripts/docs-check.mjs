import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const requiredPaths = [
  'README.md',
  'AGENTS.md',
  'docs/phases/phase-0.md',
  'docs/phases/phase-1.md',
  'docs/phases/phase-2.md',
  'docs/phases/phase-3.md',
  'docs/design/category-address-project-floorplan.md',
  'docs/design/category-style-generation.md',
  'docs/design/category-process-plan.md',
  'docs/design/category-visual-question.md',
  'docs/design/category-expert-lead.md',
  'docs/adr/ADR-0001-monorepo-provider-slice.md',
  'docs/adr/ADR-0002-project-home-read-model.md',
  'docs/adr/ADR-0003-process-plan-catalog-snapshot.md',
  'docs/adr/ADR-0004-visual-question-sync-mock-analysis.md',
  'docs/adr/ADR-0005-expert-recommendation-project-signals.md',
  'docs/adr/ADR-0006-style-image-mock-provider.md',
  'docs/issues/2026-03-28-local-env-and-secret-state.md',
  'docs/test-cases/phase-1-vertical-slice.md',
  'docs/test-cases/phase-2-process-plan.md',
  'docs/test-cases/phase-2-visual-question.md',
  'docs/test-cases/phase-2-expert-lead.md',
  'docs/test-cases/phase-3-style.md',
  'docs/runbooks/local-dev-quickstart.md',
  '.env.example',
];

const errors = [];

for (const relativePath of requiredPaths) {
  const targetPath = path.join(root, relativePath);
  if (!fs.existsSync(targetPath)) {
    errors.push(`필수 문서/파일이 없습니다: ${relativePath}`);
  }
}

const envExamplePath = path.join(root, '.env.example');
if (fs.existsSync(envExamplePath)) {
  const envExample = fs.readFileSync(envExamplePath, 'utf8');
  if (/sk-[A-Za-z0-9_-]{10,}/.test(envExample)) {
    errors.push(
      '.env.example 에 실제 비밀값처럼 보이는 문자열이 포함되어 있습니다.',
    );
  }
}

if (errors.length > 0) {
  for (const error of errors) {
    console.error(error);
  }
  process.exit(1);
}

console.log('docs:check 통과');
