import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'snapshot4s',
  tagline: 'Snapshot testing made easy',
  favicon: 'img/favicon.ico',

  url: 'https://siriusxm.github.io',
  baseUrl: '/snapshot4s/',

  // GitHub pages deployment config.
  organizationName: 'SiriusXM', // Usually your GitHub org/user name.
  projectName: 'snapshot4s', // Usually your repo name.
  deploymentBranch: 'publish-pages',

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          routeBasePath: '/',
          editUrl:
            'https://github.com/SiriusXM/snapshot4s/blob/main/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  plugins: [require.resolve('docusaurus-lunr-search')],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/docusaurus-social-card.jpg',
    navbar: {
      title: 'Snapshot4s',
      logo: {
        alt: 'Snapshot4s logo',
        src: 'img/logo-small.png',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: 'https://github.com/SiriusXM/snapshot4s/releases', 
          label: 'Changelog', 
          position: 'left'
        },
        {
          href: 'https://github.com/SiriusXM/snapshot4s',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Introduction',
              to: '/intro',
            },
            {
              label: 'Quick start',
              to: '/quick-start',
            },
          ],
        },
        {
          title: 'Community', // TODO do we want to have any other channels?
          items: [
            {
              label: 'Issues',
              href: 'https://github.com/SiriusXM/snapshot4s/issues',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/SiriusXM/snapshot4s',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} SiriusXM.`,
    },
    prism: {
      additionalLanguages: ['java', 'scala'],
      theme: prismThemes.github,
      darkTheme: prismThemes.vsDark,
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
