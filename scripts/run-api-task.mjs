import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const apiDir = path.join(root, 'apps', 'api');
const gradleArgs = process.argv.slice(2);
const preferLocalJdk = process.env.SELFINTERIOR_USE_LOCAL_JDK === '1';
const dockerGradleCommand = `chmod +x gradlew && ./gradlew --project-cache-dir /tmp/gradle-project-cache ${gradleArgs.join(' ')}`;

if (gradleArgs.length === 0) {
  console.error('실행할 Gradle task를 전달해야 합니다.');
  process.exit(1);
}

const bundledJdkDir = findBundledJdk(root);
const result =
  preferLocalJdk && bundledJdkDir
    ? spawnSync(
        'cmd.exe',
        ['/d', '/s', '/c', `gradlew.bat ${gradleArgs.join(' ')}`],
        {
          cwd: apiDir,
          stdio: 'inherit',
          env: {
            ...process.env,
            JAVA_HOME: bundledJdkDir,
            PATH: `${path.join(bundledJdkDir, 'bin')};${process.env.PATH ?? ''}`,
          },
        },
      )
    : spawnSync(
        'docker',
        [
          'run',
          '--rm',
          '-v',
          `${root}:/workspace`,
          '-v',
          'selfinterior-gradle-cache:/home/gradle/.gradle',
          '-w',
          '/workspace/apps/api',
          'gradle:8.14.3-jdk21',
          'bash',
          '-lc',
          dockerGradleCommand,
        ],
        {
          cwd: apiDir,
          stdio: 'inherit',
          env: process.env,
        },
      );

if (result.status !== 0) {
  process.exit(result.status ?? 1);
}

function findBundledJdk(currentRoot) {
  const toolsDir = path.join(currentRoot, '.tools');
  if (!fs.existsSync(toolsDir)) {
    return null;
  }

  const entries = fs
    .readdirSync(toolsDir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && entry.name.startsWith('jdk-'))
    .sort((left, right) => right.name.localeCompare(left.name));

  return entries.length > 0 ? path.join(toolsDir, entries[0].name) : null;
}
