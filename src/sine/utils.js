const assign = (val, obj, ...path) => {
  let curr = obj;
  let lastIdx = path.length - 1;
  for (let v of path.slice(0, lastIdx)) {
    if (!curr[v]) curr[v] = {};
    curr = curr[v];
  }
  curr[path[lastIdx]] = val;
  return val;
};

const access = (obj, ...path) => {
  let curr = obj;
  let lastIdx = path.length - 1;
  for (let v of path.slice(0, lastIdx)) {
    curr = curr[v];
  }
  return curr[path[lastIdx]];
};

export { assign, access };
