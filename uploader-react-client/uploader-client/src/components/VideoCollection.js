import React, { Component } from "react";
import VideoCard from "./VideoCard";
//import { fadeIn } from 'react-animations'
//import {TransitionGroup} from 'react-transition-group'
import "react-sticky-header/styles.css";
import StickyHeader from "react-sticky-header";
import {
  MDBDropdown,
  MDBDropdownToggle,
  MDBDropdownMenu,
  MDBDropdownItem,
} from "mdbreact";
import "../App.css";
import FadeIn from "react-fade-in";

const savePostRequest = {
  method: "POST",
  headers: { "Content-Type": "application/json" },
};

//let currentChannel = React.createContext('channel'); //@todo need to make current channel available to the cards for url requests

class VideoCollection extends Component {
  constructor(props) {
    super(props);

    this.state = {
      videos: [],
      channelList: [],
      vidListSize: "",
      timeOfLastAddedVid: "",
      currentChannel: "",
    };

    this.unmountCard = this.unmountCard.bind(this);
    this.updateTitleAndDesc = this.updateTitleAndDesc.bind(this);
    this.fetchFromServer = this.fetchFromServer.bind(this);
  }

  async fetchFromServer(channelName) {
    //I know this is a hacky way to fetch; make it better by all means; PRs are very welcome
    await fetch("http://10.0.0.20:8080/react/get_channel_list")
      .then((res) => res.json())
      .then((data) => this.setState({ channelList: data }))
      .catch((e) => alert("Failed to get channel list\n\n" + e));

    await fetch("http://10.0.0.20:8080/" + channelName + "/react/videos") //consider to defaulting to first item in list
      .then((res) => res.json())
      .then((data) => this.setState({ videos: data }))
      .catch((e) => {
        alert("Failed to fetch videos\n\n" + e);
        this.setState({
          videos: [],
          vidListSize: 0,
          timeOfLastAddedVid: "?",
        });
      });
    await fetch("http://10.0.0.20:8080/" + channelName + "/react/title")
      .then((res) => res.json())
      .then((data) =>
        this.setState({
          vidListSize: data.numInQueue,
          timeOfLastAddedVid: data.timeOfLastAddedVid,
        })
      )
      .catch((e) => alert("Failed to fetch title info\n\n" + e));
    await fetch(
      "http://10.0.0.20:8080/react/set_most_recent_channel/" + channelName,
      savePostRequest
    ).catch((e) => "Failed to set most recent channel\n\n" + e);

    this.setState({ currentChannel: channelName });

    // this.setState({currentChannel: mostRecentChannel})
    //set most recent channel in redis
  }

  componentDidMount() {
    fetch("http://10.0.0.20:8080/react/get_most_recent_channel")
      .then((res) => res.json())
      .then((data) => this.fetchFromServer(data.channel)) //.setState({currentChannel: data.channel}))

      //.then(this.fetchFromServer())

      //.then(this.fetchFromServer(this.state.currentChannel))
      .catch((e) => alert("Failed to fetch current channel data\n\n " + e));
  }

  updateTitleAndDesc(
    title,
    description,
    vidNumber,
    keywords,
    privacyStatus,
    playlist,
    thumbnail,
    category
  ) {
    let tempVideos = this.state.videos;
    const objIndex = tempVideos.findIndex((obj) => obj.vidNumber === vidNumber);
    tempVideos[objIndex].title = title;
    tempVideos[objIndex].description = description;
    tempVideos[objIndex].keywords = keywords;
    tempVideos[objIndex].privacyStatus = privacyStatus;
    tempVideos[objIndex].playlist = playlist;
    tempVideos[objIndex].thumbnail = thumbnail;
    tempVideos[objIndex].category = category;

    this.setState({ videos: tempVideos });
  }

  unmountCard(cardNumber) {
    try {
      const newVidCollection = this.state.videos.filter(
        (video) => video.vidNumber !== cardNumber
      );
      this.setState({
        videos: newVidCollection,
        numInQueue: this.state.numInQueue - 1,
        vidListSize: this.state.vidListSize - 1
      });
    } catch (e) {
      alert("something went wrong");
      console.log(e);
    }
  }

  render() {
    return (
      <div id="container">
        <StickyHeader
          // This is the sticky part of the header.
          header={
            <div className="Header_root">
              <MDBDropdown style={{ float: "left" }} className="ml-2 mt-2">
                <MDBDropdownToggle caret color="primary">
                  {this.state.currentChannel}
                </MDBDropdownToggle>
                <MDBDropdownMenu basic>
                  {this.state.channelList.map((channelName) => (
                    <MDBDropdownItem
                      key={channelName}
                      onClick={() => {
                        this.fetchFromServer(channelName);
                        window.scroll({ top: 0, left: 0, behavior: "smooth" });
                      }}
                    >
                      {channelName}
                    </MDBDropdownItem>
                  ))}
                </MDBDropdownMenu>
              </MDBDropdown>
            </div>
          }
        ></StickyHeader>
        <div style={{ display: "inline" }}>
          {/* <h1 style={{ float: "right", display: "inline", color: "white" }}>
          {this.state.currentChannel}
          </h1> */}

          <div>
            <div id="list-title">
              {" "}
              {this.state.vidListSize} videos in queue{" "}
            </div>
            <div id="list-subtitle">
              Last video added: {this.state.timeOfLastAddedVid}
            </div>
          </div>
        </div>

        {this.state.videos.map((video) => (
          <FadeIn key={video.vidNumber}>
            <VideoCard
              updateTitleAndDesc={this.updateTitleAndDesc}
              unmount={this.unmountCard}
              video={video}
              key={video.vidNumber}
              channel={this.state.currentChannel}
            />
          </FadeIn>
        ))}
      </div>
    );
  }
}

export default VideoCollection;
