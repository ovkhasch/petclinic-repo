/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.owner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerController;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 */
@RunWith(SpringRunner.class)
@WebMvcTest(OwnerController.class)
public class OwnerControllerTests {

    private static final int TEST_OWNER_ID = 1;
    private static final String OWNERS_NEW_URL = "/owners/new";

    private static final String OWNER_ATTRIBUTE = "owner";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository owners;

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String GEORGE = "George";
    private static final String FRANKLIN = "Franklin";
    private static final String LIBERTY_STREET_ADDRESS = "110 W. Liberty St.";
    private static final String MADISON = "Madison";
    private static final String GEORGE_PHONE_NUMBER = "6085551023";
    private static final String CREATE_OR_UPDATE_OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String LAST_NAME_BLOGGS = "Bloggs";
    private static final String ADDRESS = "address";
    private static final String LONDON = "London";
    private static final String TELEPHONE = "telephone";
    private static final String OWNERS_URL = "/owners";
    private static final String OWNER_EDIT_URL = "/owners/{ownerId}/edit";
    private Owner george;

    @Before
    public void setup() {
        george = new Owner();
        george.setId(TEST_OWNER_ID);
        george.setFirstName(GEORGE);
        george.setLastName(FRANKLIN);
        george.setAddress(LIBERTY_STREET_ADDRESS);
        george.setCity(MADISON);
        george.setTelephone(GEORGE_PHONE_NUMBER);
        given(this.owners.findById(TEST_OWNER_ID)).willReturn(george);
    }

    @Test
    public void testInitCreationForm() throws Exception {
        mockMvc.perform(get(OWNERS_NEW_URL))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists(OWNER_ATTRIBUTE))
            .andExpect(view().name(CREATE_OR_UPDATE_OWNER_FORM));
    }

    @Test
    public void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post(OWNERS_NEW_URL)
            .param(FIRST_NAME, "Joe")
            .param(LAST_NAME, LAST_NAME_BLOGGS)
            .param(ADDRESS, "123 Caramel Street")
            .param("city", LONDON)
            .param(TELEPHONE, "01316761638")
        )
            .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post(OWNERS_NEW_URL)
            .param(FIRST_NAME, "Joe")
            .param(LAST_NAME, LAST_NAME_BLOGGS)
            .param("city", LONDON)
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors(OWNER_ATTRIBUTE))
            .andExpect(model().attributeHasFieldErrors(OWNER_ATTRIBUTE, ADDRESS))
            .andExpect(model().attributeHasFieldErrors(OWNER_ATTRIBUTE, TELEPHONE))
            .andExpect(view().name(CREATE_OR_UPDATE_OWNER_FORM));
    }

    @Test
    public void testInitFindForm() throws Exception {
        mockMvc.perform(get("/owners/find"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    public void testProcessFindFormSuccess() throws Exception {
        given(this.owners.findByLastName("")).willReturn(Lists.newArrayList(george, new Owner()));
        mockMvc.perform(get(OWNERS_URL))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/ownersList"));
    }

    @Test
    public void testProcessFindFormByLastName() throws Exception {
        given(this.owners.findByLastName(george.getLastName())).willReturn(Lists.newArrayList(george));
        mockMvc.perform(get(OWNERS_URL)
            .param(LAST_NAME, FRANKLIN)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
    }

    @Test
    public void testProcessFindFormNoOwnersFound() throws Exception {
        mockMvc.perform(get(OWNERS_URL)
            .param(LAST_NAME, "Unknown Surname")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("owner", "lastName"))
            .andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    public void testInitUpdateOwnerForm() throws Exception {
        mockMvc.perform(get(OWNER_EDIT_URL, TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists(OWNER_ATTRIBUTE))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty("lastName", is(FRANKLIN))))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty(FIRST_NAME, is(GEORGE))))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty(ADDRESS, is(LIBERTY_STREET_ADDRESS))))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty("city", is(MADISON))))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty(TELEPHONE, is(GEORGE_PHONE_NUMBER))))
            .andExpect(view().name(CREATE_OR_UPDATE_OWNER_FORM));
    }

    @Test
    public void testProcessUpdateOwnerFormSuccess() throws Exception {
        mockMvc.perform(post(OWNER_EDIT_URL, TEST_OWNER_ID)
            .param(FIRST_NAME, "Joe")
            .param("lastName", LAST_NAME_BLOGGS)
            .param("address", "123 Caramel Street")
            .param("city", LONDON)
            .param(TELEPHONE, "01616291589")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    public void testProcessUpdateOwnerFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
            .param("firstName", "Joe")
            .param(LAST_NAME, "Bloggs")
            .param("city", LONDON)
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("owner"))
            .andExpect(model().attributeHasFieldErrors("owner", ADDRESS))
            .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
            .andExpect(view().name(CREATE_OR_UPDATE_OWNER_FORM));
    }

    @Test
    public void testShowOwner() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty("lastName", is("Franklin"))))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty(FIRST_NAME, is("George"))))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty(ADDRESS, is("110 W. Liberty St."))))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty("city", is("Madison"))))
            .andExpect(model().attribute(OWNER_ATTRIBUTE, hasProperty(TELEPHONE, is("6085551023"))))
            .andExpect(view().name("owners/ownerDetails"));
    }

}
