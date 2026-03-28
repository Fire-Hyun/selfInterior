import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const apiDir = path.join(root, 'apps', 'api');
const gradleArgs = process.argv.slice(2);
const localJdkDir = path.join(root, '.tools', 'jdk-21.0.10+7');

if (gradleArgs.length === 0) {
  console.error('실행할 Gradle task를 전달해야 합니다.');
  process.exit(1);
}

const result = fs.existsSync(localJdkDir)
  ? spawnSync(
      'cmd.exe',
      ['/d', '/s', '/c', `gradlew.bat ${gradleArgs.join(' ')}`],
      {
        cwd: apiDir,
        stdio: 'inherit',
        env: {
          ...process.env,
          JAVA_HOME: localJdkDir,
          PATH: `${path.join(localJdkDir, 'bin')};${process.env.PATH ?? ''}`,
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
        '-w',
        '/workspace/apps/api',
        'gradle:8.14.3-jdk21',
        'bash',
        '-lc',
        `chmod +x gradlew && ./gradlew ${gradleArgs.join(' ')}`,
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
