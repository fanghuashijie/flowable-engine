/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.service.api.runtime.process;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.*;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Process Instances" }, description = "Manage Process Instances")
public class ProcessInstanceIdentityLinkCollectionResource extends BaseProcessInstanceResource {

  @ApiOperation(value = "Get involved people for process instance", tags = { "Process Instances" }, nickname = "listProcessInstanceIdentityLinks",
          notes = "Note that the groupId in Response Body will always be null, as it’s only possible to involve users with a process-instance.")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Indicates the process instance was found and links are returned."),
          @ApiResponse(code = 404, message = "Indicates the requested process instance was not found.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/identitylinks", method = RequestMethod.GET, produces = "application/json")
  public List<RestIdentityLink> getIdentityLinks(@ApiParam(name = "processInstanceId") @PathVariable String processInstanceId, HttpServletRequest request) {
    ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);
    return restResponseFactory.createRestIdentityLinks(runtimeService.getIdentityLinksForProcessInstance(processInstance.getId()));
  }

  @ApiOperation(value = "Add an involved user to a process instance", tags = { "Process Instances" }, nickname = "createProcessInstanceIdentityLinks",
          notes = "Note that the groupId in Response Body will always be null, as it’s only possible to involve users with a process-instance.")
  @ApiResponses(value = {
          @ApiResponse(code = 201, message = "Indicates the process instance was found and the link is created."),
          @ApiResponse(code = 400, message = "Indicates the requested body did not contain a userId or a type."),
          @ApiResponse(code = 404, message = "Indicates the requested process instance was not found.")
  })
  @RequestMapping(value = "/runtime/process-instances/{processInstanceId}/identitylinks", method = RequestMethod.POST, produces = "application/json")
  public RestIdentityLink createIdentityLink(@ApiParam(name = "processInstanceId") @PathVariable String processInstanceId, @RequestBody RestIdentityLink identityLink, HttpServletRequest request, HttpServletResponse response) {

    ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

    if (identityLink.getGroup() != null) {
      throw new ActivitiIllegalArgumentException("Only user identity links are supported on a process instance.");
    }

    if (identityLink.getUser() == null) {
      throw new ActivitiIllegalArgumentException("The user is required.");
    }

    if (identityLink.getType() == null) {
      throw new ActivitiIllegalArgumentException("The identity link type is required.");
    }

    runtimeService.addUserIdentityLink(processInstance.getId(), identityLink.getUser(), identityLink.getType());

    response.setStatus(HttpStatus.CREATED.value());

    return restResponseFactory.createRestIdentityLink(identityLink.getType(), identityLink.getUser(), identityLink.getGroup(), null, null, processInstance.getId());
  }
}