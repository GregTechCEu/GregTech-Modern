import React from "react";
import Link from "@docusaurus/Link";

export default function Card(props: any) {
  return (
    <a href={props.link} className="no-underline">
      <div className="card link-card">
        <div className="card__header">
          <h3>{props.title}</h3>
        </div>
        <div className="card__body">
          <p>
            {props.body}
          </p>
        </div>
        {(props.link != undefined) ? <div className="card__footer">
          <Link isNavLink={true} to={props.link} className="button button--secondary button--block">
            {props.linkTitle}
          </Link>
        </div> : ''}
      </div>
    </a>
  );
}
