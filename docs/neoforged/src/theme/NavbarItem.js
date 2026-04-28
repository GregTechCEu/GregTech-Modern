import React from "react";
import OriginalNavBarItem from '@theme-original/NavbarItem';
import {useActiveDocContext} from '@docusaurus/plugin-content-docs/client';

export default function NavbarItem(props) {
  const { docsPluginId, type } = props
  // Workaround for https://github.com/facebook/docusaurus/issues/4389
  const { activeDoc } = useActiveDocContext(docsPluginId);
  if (type === 'docsVersionDropdown' && !activeDoc) {
    return null;
  }
  return <OriginalNavBarItem {...props} />;
}