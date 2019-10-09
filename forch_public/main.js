
const data = {}
let editor = null;

function load_viewer() {
  const container = document.getElementById('viewer_container')
  const options = {
    mode: 'view'
  };
  editor = new JSONEditor(container, options);
}

function data_update(category, new_data, func) {
  console.log('Updating', category, 'with', new_data);
  console.log(JSON.stringify(new_data, null, 2));
  data[category] = new_data;
  editor.set(data);
  func && func();
}

function fetch_data(category, data_url, func) {
  console.log('Fetching', data_url)
  fetch(data_url)
    .then(response => response.json())
    .then(json => data_update(category, json, func))
    .catch(rejection => console.log(rejection));
}

function find_t1_switches(switch_names) {
  const result = [];
  for (const switch_name of switch_names) {
    const t1index = switch_name.indexOf('t1');
    const switch_type = t1index >= 0 && switch_name.substring(t1index, t1index + 2);
    if (switch_type == 't1') {
      result.push(switch_name);
    }
  }
  return result.sort();
}

function find_t2_switches(switch_names) {
  const result = [];
  for (const switch_name of switch_names) {
    const t1index = switch_name.indexOf('t2');
    const switch_type = t1index >= 0 && switch_name.substring(t1index, t1index + 2);
    if (switch_type == 't2') {
      result.push(switch_name);
    }
  }
  return result.sort();
}

function render_t1_switches(switch_left, switch_right) {
  const template = document.getElementById('switch_dist_row_template');
  const table_row_html = template.innerHTML;
  const table_row = eval('`' + table_row_html + '`');
  const target_element = document.querySelector('#switch_table tbody');
  const tbody_holder = document.createElement(target_element.tagName)
  tbody_holder.innerHTML = table_row;
  const row_element = tbody_holder.firstElementChild;
  target_element.appendChild(row_element);
}

function render_t2_switch(switch_name) {
  const template = document.getElementById('switch_access_row_template');
  const table_row_html = template.innerHTML;
  const table_row = eval('`' + table_row_html + '`');
  const target_element = document.querySelector('#switch_table tbody');
  const tbody_holder = document.createElement(target_element.tagName)
  tbody_holder.innerHTML = table_row;
  const row_element = tbody_holder.firstElementChild;
  target_element.appendChild(row_element);
}

function populate_table() {
  console.log('Populating switches', data.switches);
  switch_names = data.switches && Object.keys(data.switches)
  if (!switch_names) {
    document.getElementById('switch_table').innerHTML = 'No switches to be found!';
    return;
  }

  console.log('switch_names', switch_names)
  for (const switch_name of switch_names) {
    fetch_data(`switch_${switch_name}`, `switch?switch_name=${switch_name}`);
  }

  t1_switches = find_t1_switches(switch_names);
  console.log('t1_switches', t1_switches);
  render_t1_switches(t1_switches[0], t1_switches[1]);

  t2_switches = find_t2_switches(switch_names);
  console.log('t2_switches', t2_switches);
  for (const switch_name of t2_switches) {
    render_t2_switch(switch_name);
  }
}

function initialize() {
  console.log('initializing viewer');
  load_viewer();
  fetch_data('overview', 'overview');
  fetch_data('topology', 'topology');
  fetch_data('switches', 'switches', populate_table);
}