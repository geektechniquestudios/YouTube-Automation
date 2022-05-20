import React, { Component } from 'react'
import TextTruncate from 'react-text-truncate'
import { Spinner, Card } from 'react-bootstrap'
import swal from 'sweetalert'
import { MDBContainer, MDBInput, MDBBtn } from 'mdbreact'


const deleteRequest = {
  method: 'DELETE',
  headers: { 'Content-Type': 'application/json' }
}
const uploadPostRequest = {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' }
}

const requestMapping = 'http://10.0.0.20:8080/'

class VideoCard extends Component {

  constructor(props) {
    super(props)

    this.state = {
      uploadLoading: false,
      deleteLoading: false,
      saveLoading: false,
      isBeingEdited: false,
      disabled: false,
    }

    this.uploadVideo = this.uploadVideo.bind(this)
    this.editVideoData = this.editVideoData.bind(this)
    this.deleteVideo = this.deleteVideo.bind(this)
    this.saveChanges = this.saveChanges.bind(this)
    this.cancelChanges = this.cancelChanges.bind(this)
    this.getFormData = this.getFormData.bind(this)
  }

  componentDidMount() {
    this.setState({
      newTitle: this.props.video.title,
      newDescription: this.props.video.description,
    })
  }

  uploadVideo(vidNumber) {
    this.setState({
      uploadLoading: true,
      disabled: true
    })

    fetch(requestMapping + this.props.channel + "/react/upload/" + vidNumber, uploadPostRequest)
      .then(response => response.json())
      .then((data) => {
        if (data.operationSuccess === true) {
          swal(
            "Success!",
            "Video " + data.vidNumber + " uploaded.",
            "success"
          )
          this.props.unmount(this.props.video.vidNumber)
        } else {
          swal(
            "Server Error!",
            "Failed to upload video " +
            data.vidNumber + "." +
            (data.message == null ? "" : " " + data.message),
            "error"
          )

          this.setState({
            uploadLoading: false,
            disabled: false
          })
        }
      })
      .catch(e => {
        console.warn("error: " + e)
        swal(
          "Connection Error!",
          "Failed to upload video " + this.props.video.vidNumber + ".",
          "error"
        )
        this.setState({
          uploadLoading: false,
          disabled: false
        })
      })
  }

  editVideoData() {
    this.setState({
      isBeingEdited: true,
    })
  }

  deleteVideo(vidNumber) {
    this.setState({
      deleteLoading: true,
      disabled: true
    })

    swal({
      title: "Are you sure?",
      text: "Deleting this can't be undone!",
      icon: "warning",
      buttons: [
        'Keep Video',
        'Delete Video Permanently'
      ],
      dangerMode: true,
    }).then((isConfirm) => {
      if (isConfirm) {
        fetch(requestMapping + this.props.channel + "/react/delete/" + vidNumber, deleteRequest)
          .then(response => response.json())
          .then((data) => {
            if (data.operationSuccess === true) {
              swal(
                "Success!",
                "Video " + data.vidNumber + " deleted.",
                "success"
              )
              this.props.unmount(this.props.video.vidNumber)
            } else {
              swal(
                "Server Error!",
                "Failed to deleted video " +
                data.vidNumber + "." +
                (data.message == null ? "" : " " + data.message),
                "error"
              )
              this.setState({
                deleteLoading: false,
                disabled: false
              })
            }
          })
          .catch(e => {
            console.warn("error: " + e)
            swal(
              "Connection Error!",
              "Failed to delete video " + this.props.video.vidNumber + ".",
              "error"
            )
          })
      }
    })

    this.setState({
      deleteLoading: false,
      disabled: false,
    })
  }

  saveChanges() {
    this.setState({
      saveLoading: true,
      disabled: true
    })

    const formData = this.getFormData()
    const savePostRequest = {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData)
    }

    fetch(requestMapping + this.props.channel + "/react/edit/", savePostRequest)
      .then(response => response.json())
      .then((data) => {
        if (data.operationSuccess === true) {
          swal(
            "Success!",
            "Video " + data.vidNumber + " updated.",
            "success"
          )
          this.setState({
            saveLoading: false,
            disabled: false,
            isBeingEdited: false
          })


          this.props.updateTitleAndDesc(
            this.state.newTitle,
            this.state.newDescription,
            this.props.video.vidNumber,
            formData.keywords,
            formData.privacyStatus,
            formData.playlist,
            formData.thumbnail,
            formData.category
          )
        } else {
          swal(
            "Server Error!",
            "Failed to update video " +
            data.vidNumber + "." +
            (data.message == null ? "" : " " + data.message),
            "error"
          )
          this.setState({
            saveLoading: false,
            disabled: false
          })
        }
      })
      .catch(e => {
        console.warn("error: " + e)
        swal(
          "Connection Error!",
          "Failed to update video " + this.props.video.vidNumber + ".",
          "error"
        )
        this.setState({
          saveLoading: false,
          disabled: false
        })
      })
  }

  cancelChanges() {
    swal({
      title: "Are you sure?",
      text: "Changes won't be saved!",
      icon: "warning",
      buttons: [
        'Keep Editing',
        'Discard Changes'
      ],
      dangerMode: true,
    }).then((isConfirm) => {
      if (isConfirm) {
        this.setState({
          isBeingEdited: false,
          newTitle: this.props.video.title,
          newDescription: this.props.video.description
        })
      }
    })
  }

  getFormData() {
    this.formData = {
      vidNumber: this.props.video.vidNumber,
      category: this.newCategory.value,
      title: this.state.newTitle,
      description: this.state.newDescription,
      keywords: this.newKeywords.value,
      privacyStatus: this.newPrivacyStatus.value,
      playlist: this.newPlaylist.value,
      thumbnail: this.newThumbnail.value
    }

    return this.formData
  }

  render() {
    return (
      <>
        <Card bg="dark" border="info" text="light" id="card">
          <Card.Header as="h5" >{this.props.video.vidNumber}</Card.Header>
          <Card.Body>
            <div id="main-card-body">
              <div id="text-group">
                <Card.Subtitle as="h5" style={{ color: "#a6a6a6" }}>{this.state.newTitle}</Card.Subtitle>
                <TextTruncate
                  line={3}
                  truncateText="..."
                  className="card-text"
                  text={this.state.newDescription}
                />
              </div>
            </div>

            <div className="vid-button-group">
              {this.state.isBeingEdited ?

                <div>
                  <MDBBtn
                    outline
                    disabled={this.state.disabled}
                    color="success"
                    className="vid-button"
                    onClick={() => this.saveChanges()}
                  >
                    <span className="button-text">Save</span>
                    {this.state.saveLoading ?
                      <div>
                        <Spinner
                          variant="success"
                          as="span"
                          animation="grow"
                          role="status"
                          aria-hidden="true"
                          className="spinners"
                        /><Spinner
                          variant="success"
                          as="span"
                          animation="border"
                          role="status"
                          aria-hidden="true"
                          className="spinners"
                        />
                      </div> :
                      null}
                  </MDBBtn>

                  <MDBBtn
                    outline
                    disabled={this.state.disabled}
                    color="danger"
                    className="vid-button"
                    onClick={() => this.cancelChanges()}
                  >
                    <span className="button-text">Cancel</span>
                  </MDBBtn>
                </div>

                :

                <div>
                  <MDBBtn
                    outline
                    disabled={this.state.disabled}
                    color="primary"
                    className="vid-button"
                    onClick={() => this.uploadVideo(this.props.video.vidNumber)}
                  >
                    <span className="button-text">Upload</span>
                    {this.state.uploadLoading ?
                      <div>
                        <Spinner
                          variant="primary"
                          as="span"
                          animation="grow"
                          role="status"
                          aria-hidden="true"
                          className="spinners"
                        /><Spinner
                          variant="primary"
                          as="span"
                          animation="border"
                          role="status"
                          aria-hidden="true"
                          className="spinners"
                        /> </div> :
                      null}
                  </MDBBtn>

                  <MDBBtn
                    outline
                    disabled={this.state.disabled}
                    color="info"
                    type="button"
                    className="vid-button"
                    onClick={() => this.editVideoData()}
                  >
                    <span className="button-text">Edit</span>
                  </MDBBtn>

                  <MDBBtn
                    outline
                    disabled={this.state.disabled}
                    color="danger"
                    type="button"
                    className="vid-button"
                    onClick={() => this.deleteVideo(this.props.video.vidNumber)}
                  >
                    <span className="button-text">Delete</span>
                    {this.state.deleteLoading ?
                      <div>
                        <Spinner
                          variant="danger"
                          as="span"
                          animation="grow"
                          role="status"
                          aria-hidden="true"
                          className="spinners"
                        /><Spinner
                          variant="danger"
                          as="span"
                          animation="border"
                          role="status"
                          aria-hidden="true"
                          className="spinners"
                        /></div> :
                      null}
                  </MDBBtn>
                </div>}

            </div>
          </Card.Body>
          {this.state.isBeingEdited ?
            <MDBContainer>

              <MDBInput
                material
                className="mb-3 mt-0 text-light"
                label="Title"
                disabled={this.state.disabled}
                onChange={e => this.setState({ newTitle: e.target.value })}
                valueDefault={this.props.video.title}
              />

              <MDBInput
                material
                className="mb-3 mt-0 text-light"
                label="Description"
                type="textarea"
                disabled={this.state.disabled}
                onChange={e => this.setState({ newDescription: e.target.value })}
                style={{ height: 120 }}
                valueDefault={this.props.video.description}
              />

              <MDBInput
                material
                className="mb-3 mt-0 text-light"
                label="Tags"
                disabled={this.state.disabled}
                inputRef={(ref) => this.newKeywords = ref}
                valueDefault={this.props.video.keywords}
              />

              <MDBInput
                material
                className="mb-3 mt-0 text-light"
                label="Category"
                disabled={this.state.disabled}
                inputRef={(ref) => this.newCategory = ref}
                valueDefault={this.props.video.category}
              />

              <MDBInput
                material
                className="mb-3 mt-0 text-light"
                label="Privacy Status"
                disabled={this.state.disabled}
                inputRef={(ref) => this.newPrivacyStatus = ref}
                valueDefault={this.props.video.privacyStatus} //@todo -> make into selector
              />

              <MDBInput
                material
                className="mb-3 mt-0 text-light"
                label="Playlist"
                disabled={this.state.disabled}
                inputRef={(ref) => this.newPlaylist = ref}
                valueDefault={this.props.video.playlist}
              />

              <MDBInput
                material
                className="mb-3 mt-0 text-light"
                label="Thumbnail"
                disabled={this.state.disabled}
                inputRef={(ref) => this.newThumbnail = ref}
                valueDefault={this.props.video.thumbnail}
              />

            </MDBContainer>
            :
            null}
        </Card>
      </>
    )
  }
}

export default VideoCard