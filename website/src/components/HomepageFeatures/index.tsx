import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

type FeatureItemX = {
  title: string;
  emojiIcon: string;
  description: JSX.Element;
};

const FeatureList: FeatureItemX[] = [
  {
    title: 'Batteries Included',
    emojiIcon: '💡',
    description: (
      <>
	It is as simple as adding an sbt plugin.
      </>
    ),
  },
  {
    title: 'Powerful',
    emojiIcon: '🚀',
    description: (
      <>
	Automate your test updates for any ADT.
      </>
    ),
  },
  {
    title: 'Flexible',
    emojiIcon: '🔌',
    description: (
      <>
	Supports inline and file snapshots.
      </>
    ),
  }
];

function Feature({title, emojiIcon, description}: FeatureItemX) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <p className={styles.emojiIcon}>{emojiIcon}</p>
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
