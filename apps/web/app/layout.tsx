import type { Metadata } from 'next';
import { IBM_Plex_Sans_KR, Roboto_Mono } from 'next/font/google';
import './globals.css';

const headingFont = IBM_Plex_Sans_KR({
  variable: '--font-heading',
  subsets: ['latin'],
  weight: ['400', '500', '600', '700'],
});

const monoFont = Roboto_Mono({
  variable: '--font-mono',
  subsets: ['latin'],
  weight: ['400', '500'],
});

export const metadata: Metadata = {
  title: 'selfInterior | Address-first MVP',
  description:
    '주소 검색부터 프로젝트 생성과 도면 후보 저장까지 연결하는 첫 vertical slice',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="ko"
      className={`${headingFont.variable} ${monoFont.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
