# Copyright 2017-2019 EPAM Systems, Inc. (https://www.epam.com/)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import os

import pykube
from azure.common.client_factory import get_client_from_auth_file, get_client_from_cli_profile
from azure.mgmt.resource import ResourceManagementClient
from azure.mgmt.network import NetworkManagementClient
from azure.mgmt.compute import ComputeManagementClient
from azure.mgmt.resource.managedapplications.models import GenericResource

RUN_ID_LABEL = 'runid'
CLOUD_REGION_LABEL = 'cloud_region'

auth_file = os.environ.get('AZURE_AUTH_LOCATION', None)
if auth_file:
    res_client = get_client_from_auth_file(ResourceManagementClient, auth_path=auth_file)
    network_client = get_client_from_auth_file(NetworkManagementClient, auth_path=auth_file)
    compute_client = get_client_from_auth_file(ComputeManagementClient, auth_path=auth_file)
else:
    res_client = get_client_from_cli_profile(ResourceManagementClient)
    network_client = get_client_from_cli_profile(NetworkManagementClient)
    compute_client = get_client_from_cli_profile(ComputeManagementClient)

resource_group_name = os.environ["AZURE_RESOURCE_GROUP"]


def resolve_azure_api(resource):
    """ This method retrieves the latest non-preview api version for
    the given resource (unless the preview version is the only available
    api version) """
    provider = res_client.providers.get(resource.id.split('/')[6])
    rt = next((t for t in provider.resource_types
               if t.resource_type.lower() == '/'.join(resource.type.split('/')[1:]).lower()), None)
    if rt and 'api_versions' in rt.__dict__:
        api_version = [v for v in rt.__dict__['api_versions'] if 'preview' not in v.lower()]
        return api_version[0] if api_version else rt.__dict__['api_versions'][0]


def get_instance_name_and_private_ip_from_vmss(scale_set_name):
    vm_vmss_id = None
    for vm in compute_client.virtual_machine_scale_set_vms.list(resource_group_name, scale_set_name):
        vm_vmss_id = vm.instance_id
        break
    instance_name = compute_client.virtual_machine_scale_set_vms \
        .get_instance_view(resource_group_name, scale_set_name, vm_vmss_id) \
        .additional_properties["computerName"]
    private_ip = network_client.network_interfaces. \
        get_virtual_machine_scale_set_ip_configuration(resource_group_name, scale_set_name, vm_vmss_id,
                                                       scale_set_name + "-nic", scale_set_name + "-ip") \
        .private_ip_address
    return instance_name, private_ip


def find_and_tag_instance(old_id, new_id):
    ins_id = None
    retrieved_resources = []

    # first let's filter resources with specific tag and load resource fully with all info
    # by res_client.resources.get_by_id
    # if we can't load one of resources from the filtered list - we will fail before we try to change a tag value
    for resource in res_client.resources.list(filter="tagName eq 'Name' and tagValue eq '" + old_id + "'"):
        resource_type = str(resource.type).split('/')[-1]
        if resource_type.lower() == "virtualmachines":
            ins_id = resource.name
        elif str(resource.type).split('/')[-1].lower() == "virtualmachinescalesets":
            ins_id, _ = get_instance_name_and_private_ip_from_vmss(resource.name)
        az_api_version = resolve_azure_api(resource)
        loaded_resource = res_client.resources.get_by_id(resource.id, az_api_version)
        if not loaded_resource:
            raise RuntimeError("Failed to load resource by id {}".format(resource.id))
        retrieved_resources.append(loaded_resource)
    if not ins_id:
        raise RuntimeError("Failed to find instance {}".format(old_id))
    # after all resources are successfully loaded - let's change a tag
    for resource in retrieved_resources:
        az_api_version = resolve_azure_api(resource)
        if not resource.tags:
            resource.tags = {}
        resource.tags["Name"] = new_id
        res_client.resources.update_by_id(resource.id, az_api_version,
                                          GenericResource(tags=resource.tags))
    return ins_id


def verify_regnode(kube_api, ins_id):
    exist_node = False
    ret_namenode = ""
    node = pykube.Node.objects(kube_api).filter(field_selector={'metadata.name': ins_id})
    if len(node.response['items']) > 0:
        exist_node = True
        ret_namenode = ins_id
    if not exist_node:
        raise RuntimeError("Failed to find Node {}".format(ins_id))
    return ret_namenode


def get_instance_name_and_private_ip_from_vmss(scale_set_name):
    vm_vmss_id = None
    for vm in compute_client.virtual_machine_scale_set_vms.list(resource_group_name, scale_set_name):
        vm_vmss_id = vm.instance_id
        break
    instance_name = compute_client.virtual_machine_scale_set_vms \
        .get_instance_view(resource_group_name, scale_set_name, vm_vmss_id) \
        .additional_properties["computerName"]
    private_ip = network_client.network_interfaces\
        .get_virtual_machine_scale_set_ip_configuration(resource_group_name,
                                                        scale_set_name, vm_vmss_id,
                                                        scale_set_name + "-nic",
                                                        scale_set_name + "-ip") \
        .private_ip_address
    return instance_name, private_ip


def change_label(api, nodename, new_id, cloud_region):
    obj = {
        "apiVersion": "v1",
        "kind": "Node",
        "metadata": {
            "name": nodename,
            "labels": {
                RUN_ID_LABEL: new_id
            }
        }
    }
    node = pykube.Node(api, obj)
    node.labels[RUN_ID_LABEL] = new_id
    node.labels[CLOUD_REGION_LABEL] = cloud_region
    node.update()


def get_cloud_region(api, run_id):
    nodes = pykube.Node.objects(api).filter(selector={RUN_ID_LABEL: run_id})
    if len(nodes.response['items']) == 0:
        raise RuntimeError('Cannot find node matching RUN ID %s' % run_id)
    node = nodes.response['items'][0]
    labels = node['metadata']['labels']
    if CLOUD_REGION_LABEL not in labels:
        raise RuntimeError('Node %s is not labeled with Cloud Region' % node['metadata']['name'])
    return labels[CLOUD_REGION_LABEL]


def get_kube_api():
    try:
        api = pykube.HTTPClient(pykube.KubeConfig.from_service_account())
    except Exception as e:
        api = pykube.HTTPClient(pykube.KubeConfig.from_file("~/.kube/config"))
    api.session.verify = False
    return api


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--old_id", "-kid", type=str, required=True)
    parser.add_argument("--new_id", "-nid", type=str, required=True)
    args, unknown = parser.parse_known_args()
    old_id = args.old_id
    new_id = args.new_id

    kube_api = get_kube_api()
    cloud_region = get_cloud_region(kube_api, old_id)

    ins_id = find_and_tag_instance(old_id, new_id)
    nodename = verify_regnode(kube_api, ins_id)
    change_label(kube_api, nodename, new_id, cloud_region)


if __name__ == '__main__':
    main()
