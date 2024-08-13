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
              {/* TODO: write something meaningful before merge */}
                Aut suscipit doloremque omnis pariatur. Similique ut incidunt totam. Earum distinctio nihil quasi eum occaecati deserunt.

Eius rerum vitae eos repellat impedit ea. Nesciunt itaque qui et. Maxime consequatur rerum hic tempore necessitatibus quam id. Tempore dolore enim non ea autem.



              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
