import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          <div className={clsx('col col--9')}>
            <div className="text--center">
              {/* <Heading as="h2">See it in action</Heading> */}
              <video width="100%" height="auto" controls>
                <source src="https://github.com/user-attachments/assets/8e2f3037-ecb0-4f6b-a9ba-fd277d2af55d"/>
              </video>
            </div>
          </div>
          <div className={clsx('col col--3')}>
            <div className="text">
              <Heading as="h2">See it in action</Heading>
              <p>
	      Snapshot4s automates the process of writing and updating tests, reducing the maintenance burden for developers.
              </p>
	      <p>See what happens to <code>???</code> and the outdated test value on <code>snapshot4sPromote</code>. </p>
	      <p><a href="./intro">Learn more.</a></p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
